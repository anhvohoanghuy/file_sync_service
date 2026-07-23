package org.example.file_sync_service.file_context.application.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.file_sync_service.common.exception.AppException;
import org.example.file_sync_service.config.MinioProperties;
import java.io.InputStream;
import java.util.List;
import org.example.file_sync_service.file_context.application.dto.CompleteUploadRequest;
import org.example.file_sync_service.file_context.application.dto.UploadCompletionResponse;
import org.example.file_sync_service.file_context.application.dto.UploadInitiationResponse;
import org.example.file_sync_service.file_context.application.dto.UploadPartResponse;
import org.example.file_sync_service.file_context.application.dto.UploadRequest;
import org.example.file_sync_service.file_context.domain.model.aggregate.SyncedFile;
import org.example.file_sync_service.file_context.domain.model.enums.FileStatus;
import org.example.file_sync_service.file_context.domain.repository.ISyncedFileDomainRepository;
import org.example.file_sync_service.file_context.domain.service.IFileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileService {

  private final ISyncedFileDomainRepository fileRepository;
  private final IFileStorageService fileStorageService;
  private final MinioProperties minioProperties;

  /**
   * Khởi tạo một phiên upload multipart khi client yêu cầu tải file lên.
   *
   * <p>Tạo bản ghi {@link SyncedFile} ở trạng thái {@link FileStatus#INITIATED}, sinh {@code
   * objectKey} duy nhất trên MinIO và trả về {@code fileUploadId} để client dùng cho các bước gửi
   * phần dữ liệu và hoàn tất upload sau đó.
   *
   * @param userId chủ sở hữu file (lấy từ ngữ cảnh xác thực).
   * @param deviceId thiết bị nguồn gửi yêu cầu.
   * @param request thông tin file cần upload.
   * @return thông tin phiên upload vừa khởi tạo.
   */
  @Transactional
  public UploadInitiationResponse initiateMultipartUpload(
      UUID userId, String deviceId, UploadRequest request) {
    validate(userId, deviceId, request);

    String relativePath = buildRelativePath(request.path, request.fileName);
    String objectKey = buildObjectKey(userId, deviceId, request.fileName);

    // checksum chưa biết ở bước khởi tạo, sẽ được đối chiếu khi VERIFYING.
    SyncedFile file =
        SyncedFile.register(userId, deviceId, relativePath, objectKey, request.size, null);
    file.markInitiated();

    SyncedFile saved = fileRepository.save(file);

    return new UploadInitiationResponse(
        saved.getId(), saved.getObjectKey(), minioProperties.getBucket(), saved.getStatus());
  }

  /**
   * Nhận một part dữ liệu từ client trong phiên upload multipart.
   *
   * <p>Lưu part lên object storage, chuyển trạng thái phiên sang {@link FileStatus#UPLOADING} (nếu
   * đang ở INITIATED) và trả về ETag để client giữ lại phục vụ bước hoàn tất upload.
   *
   * @param userId chủ sở hữu phiên upload (lấy từ ngữ cảnh xác thực).
   * @param fileUploadId định danh phiên upload.
   * @param partNumber số thứ tự part, bắt đầu từ 1.
   * @param data luồng dữ liệu của part.
   * @param size kích thước part tính bằng byte.
   * @return thông tin part vừa nhận.
   */
  @Transactional
  public UploadPartResponse uploadPart(
      UUID userId, UUID fileUploadId, int partNumber, InputStream data, long size) {
    if (userId == null) {
      throw new AppException("USER_ID_REQUIRED", "User id is required", HttpStatus.BAD_REQUEST);
    }
    if (fileUploadId == null) {
      throw new AppException(
          "UPLOAD_ID_REQUIRED", "File upload id is required", HttpStatus.BAD_REQUEST);
    }
    if (partNumber < 1) {
      throw new AppException(
          "INVALID_PART_NUMBER", "Part number must be >= 1", HttpStatus.BAD_REQUEST);
    }
    if (data == null || size <= 0) {
      throw new AppException(
          "INVALID_PART_DATA", "Part data must not be empty", HttpStatus.BAD_REQUEST);
    }

    SyncedFile file = requireOwnedUpload(userId, fileUploadId);

    if (!file.isAcceptingParts()) {
      throw new AppException(
          "UPLOAD_NOT_ACCEPTING_PARTS",
          "Upload session cannot accept parts in status " + file.getStatus(),
          HttpStatus.CONFLICT);
    }

    String etag = fileStorageService.uploadPart(file.getObjectKey(), partNumber, data, size);

    if (file.getStatus() == FileStatus.INITIATED) {
      file.markUploading();
      fileRepository.save(file);
    }

    return new UploadPartResponse(file.getId(), partNumber, etag, file.getStatus());
  }

  /**
   * Hoàn tất phiên upload multipart: ghép các part thành object cuối cùng, xác thực checksum rồi
   * chuyển trạng thái sang {@link FileStatus#COMPLETED}.
   *
   * <p>Nếu {@code expectedChecksum} được cung cấp và không khớp với checksum thực tế của object sau
   * khi ghép, trạng thái chuyển sang {@link FileStatus#CHECKSUM_MISMATCH} và ném lỗi.
   *
   * @param userId chủ sở hữu phiên upload (lấy từ ngữ cảnh xác thực).
   * @param fileUploadId định danh phiên upload.
   * @param request danh sách part và checksum mong đợi.
   * @return thông tin file sau khi hoàn tất.
   */
  @Transactional
  public UploadCompletionResponse completeMultipartUpload(
      UUID userId, UUID fileUploadId, CompleteUploadRequest request) {
    if (userId == null) {
      throw new AppException("USER_ID_REQUIRED", "User id is required", HttpStatus.BAD_REQUEST);
    }
    if (fileUploadId == null) {
      throw new AppException(
          "UPLOAD_ID_REQUIRED", "File upload id is required", HttpStatus.BAD_REQUEST);
    }
    if (request == null || request.partNumbers() == null || request.partNumbers().isEmpty()) {
      throw new AppException(
          "PARTS_REQUIRED", "At least one part is required", HttpStatus.BAD_REQUEST);
    }

    SyncedFile file = requireOwnedUpload(userId, fileUploadId);

    if (file.getStatus() != FileStatus.UPLOADING) {
      throw new AppException(
          "UPLOAD_NOT_COMPLETABLE",
          "Upload session cannot be completed in status " + file.getStatus(),
          HttpStatus.CONFLICT);
    }

    List<Integer> partNumbers = request.partNumbers();
    String objectKey = file.getObjectKey();

    file.markVerifying();
    long size = fileStorageService.composeParts(objectKey, partNumbers);
    String actualChecksum = fileStorageService.computeChecksum(objectKey);

    String expected = request.expectedChecksum();
    if (expected != null && !expected.isBlank() && !expected.equalsIgnoreCase(actualChecksum)) {
      file.markChecksumMismatch();
      fileRepository.save(file);
      fileStorageService.deleteParts(objectKey, partNumbers);
      throw new AppException(
          "CHECKSUM_MISMATCH",
          "Checksum mismatch: expected " + expected + " but got " + actualChecksum,
          HttpStatus.CONFLICT);
    }

    file.completeUpload(size, actualChecksum);
    SyncedFile saved = fileRepository.save(file);
    fileStorageService.deleteParts(objectKey, partNumbers);

    return new UploadCompletionResponse(
        saved.getId(), saved.getObjectKey(), saved.getSize(), saved.getChecksum(),
        saved.getStatus());
  }

  /**
   * <p>Đưa bản ghi {@link SyncedFile} về trạng thái {@link FileStatus#ABORTED}. Chỉ huỷ được các
   * phiên còn đang tiến hành (INITIATED/UPLOADING/VERIFYING); nếu đã COMPLETED/AVAILABLE hoặc đã
   * huỷ/lỗi trước đó thì từ chối.
   *
   * @param userId chủ sở hữu phiên upload
   * @param fileUploadId định danh phiên upload cần huỷ.
   */
  @Transactional
  public void abortMultipartUpload(UUID userId, UUID fileUploadId) {
    if (userId == null) {
      throw new AppException("USER_ID_REQUIRED", "User id is required", HttpStatus.BAD_REQUEST);
    }
    if (fileUploadId == null) {
      throw new AppException(
          "UPLOAD_ID_REQUIRED", "File upload id is required", HttpStatus.BAD_REQUEST);
    }

    SyncedFile file = requireOwnedUpload(userId, fileUploadId);

    if (!file.isInProgress()) {
      throw new AppException(
          "UPLOAD_NOT_ABORTABLE",
          "Upload session cannot be aborted in status " + file.getStatus(),
          HttpStatus.CONFLICT);
    }

    file.markAborted();
    fileRepository.save(file);
  }

  /**
   * Lấy phiên upload theo id và đảm bảo thuộc về {@code userId}. Không tìm thấy hoặc thuộc người
   * dùng khác đều trả về NOT_FOUND để không tiết lộ sự tồn tại của phiên upload người khác.
   */
  private SyncedFile requireOwnedUpload(UUID userId, UUID fileUploadId) {
    SyncedFile file =
        fileRepository
            .findById(fileUploadId)
            .orElseThrow(
                () ->
                    new AppException(
                        "UPLOAD_NOT_FOUND", "Upload session not found", HttpStatus.NOT_FOUND));

    if (!file.getUserId().equals(userId)) {
      throw new AppException(
          "UPLOAD_NOT_FOUND", "Upload session not found", HttpStatus.NOT_FOUND);
    }
    return file;
  }

  private void validate(UUID userId, String deviceId, UploadRequest request) {
    if (userId == null) {
      throw new AppException("USER_ID_REQUIRED", "User id is required", HttpStatus.BAD_REQUEST);
    }
    if (deviceId == null || deviceId.isBlank()) {
      throw new AppException(
          "DEVICE_ID_REQUIRED", "Device id is required", HttpStatus.BAD_REQUEST);
    }
    if (request == null || request.fileName == null || request.fileName.isBlank()) {
      throw new AppException(
          "FILE_NAME_REQUIRED", "File name is required", HttpStatus.BAD_REQUEST);
    }
    if (request.size == null || request.size < 0) {
      throw new AppException(
          "INVALID_FILE_SIZE", "File size must be zero or positive", HttpStatus.BAD_REQUEST);
    }
  }

  /** Ghép path và fileName thành đường dẫn tương đối chuẩn hoá (dùng dấu "/"). */
  private String buildRelativePath(String path, String fileName) {
    if (path == null || path.isBlank()) {
      return fileName;
    }
    String normalized = path.replaceAll("/+$", "");
    return normalized + "/" + fileName;
  }

  /** Sinh khoá object duy nhất: {userId}/{deviceId}/{token}/{fileName}. */
  private String buildObjectKey(UUID userId, String deviceId, String fileName) {
    return userId + "/" + deviceId + "/" + UUID.randomUUID() + "/" + fileName;
  }
}
