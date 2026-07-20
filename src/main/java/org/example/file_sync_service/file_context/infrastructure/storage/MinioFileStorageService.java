package org.example.file_sync_service.file_context.infrastructure.storage;

import io.minio.ComposeObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SourceObject;
import io.minio.StatObjectArgs;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.file_sync_service.common.exception.AppException;
import org.example.file_sync_service.config.MinioProperties;
import org.example.file_sync_service.file_context.domain.service.IFileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Cài đặt {@link IFileStorageService} bằng MinIO. Mỗi part được lưu thành một object riêng với khoá
 * dạng {@code {objectKey}.parts/{partNumber}} để bước hoàn tất có thể ghép lại.
 */
@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements IFileStorageService {

  private static final String OCTET_STREAM = "application/octet-stream";

  private final MinioClient minioClient;
  private final MinioProperties minioProperties;

  @Override
  public String uploadPart(String objectKey, int partNumber, InputStream data, long size) {
    String partKey = partObjectKey(objectKey, partNumber);
    try {
      ObjectWriteResponse response =
          minioClient.putObject(
              PutObjectArgs.builder()
                  .bucket(minioProperties.getBucket())
                  .object(partKey)
                  .stream(data, size, -1L)
                  .contentType(OCTET_STREAM)
                  .build());
      return response.etag();
    } catch (Exception e) {
      throw new AppException(
          "PART_UPLOAD_FAILED",
          "Failed to upload part " + partNumber + ": " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public long composeParts(String objectKey, List<Integer> partNumbers) {
    String bucket = minioProperties.getBucket();
    List<SourceObject> sources =
        partNumbers.stream()
            .map(
                n ->
                    SourceObject.builder()
                        .bucket(bucket)
                        .object(partObjectKey(objectKey, n))
                        .build())
            .toList();
    try {
      minioClient.composeObject(
          ComposeObjectArgs.builder().bucket(bucket).object(objectKey).sources(sources).build());
      return minioClient
          .statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build())
          .size();
    } catch (Exception e) {
      throw new AppException(
          "COMPOSE_FAILED",
          "Failed to compose parts: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public String computeChecksum(String objectKey) {
    String bucket = minioProperties.getBucket();
    try (GetObjectResponse stream =
        minioClient.getObject(
            GetObjectArgs.builder().bucket(bucket).object(objectKey).build())) {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[8192];
      int read;
      while ((read = stream.read(buffer)) != -1) {
        digest.update(buffer, 0, read);
      }
      return "sha256:" + toHex(digest.digest());
    } catch (Exception e) {
      throw new AppException(
          "CHECKSUM_FAILED",
          "Failed to compute checksum: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void deleteParts(String objectKey, List<Integer> partNumbers) {
    String bucket = minioProperties.getBucket();
    for (int n : partNumbers) {
      try {
        minioClient.removeObject(
            RemoveObjectArgs.builder().bucket(bucket).object(partObjectKey(objectKey, n)).build());
      } catch (Exception e) {
        // Dọn dẹp không được coi là lỗi nghiệp vụ; bỏ qua để không chặn luồng hoàn tất.
      }
    }
  }

  /** Quy ước khoá object cho một part: {objectKey}.parts/{partNumber}. */
  public static String partObjectKey(String objectKey, int partNumber) {
    return objectKey + ".parts/" + partNumber;
  }

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(Character.forDigit((b >> 4) & 0xF, 16));
      sb.append(Character.forDigit(b & 0xF, 16));
    }
    return sb.toString();
  }
}
