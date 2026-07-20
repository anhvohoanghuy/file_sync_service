package org.example.file_sync_service.file_context.domain.model.aggregate;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.file_sync_service.file_context.domain.model.enums.FileStatus;

/**
 * Aggregate root của file_context: một file thuộc về một người dùng trên một thiết bị,
 * với nội dung được lưu trên MinIO qua {@code objectKey}. Mỗi lần nội dung thay đổi,
 * {@code version} tăng lên.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncedFile {

  /** Định danh nội bộ của bản ghi file. */
  private UUID id;

  /** Chủ sở hữu file — tham chiếu tới User.id của identity_context. */
  private UUID userId;

  /** Thiết bị nguồn, ví dụ "linux-laptop". */
  private String deviceId;

  /** Đường dẫn tương đối trên thiết bị, ví dụ "Documents/report.pdf". */
  private String relativePath;

  /** Khoá object trên MinIO chứa nội dung file. */
  private String objectKey;

  /** Kích thước nội dung, tính bằng byte. */
  private long size;

  /** Checksum nội dung, ví dụ "sha256:...". */
  private String checksum;

  /** Số phiên bản, bắt đầu từ 1 và tăng mỗi lần nội dung thay đổi. */
  private int version;

  /** Trạng thái vòng đời hiện tại. */
  private FileStatus status;

  private Instant createdAt;
  private Instant updatedAt;

  private SyncedFile(
      UUID userId, String deviceId, String relativePath, String objectKey, long size,
      String checksum) {
    Instant now = Instant.now();
    this.id = UUID.randomUUID();
    this.userId = userId;
    this.deviceId = deviceId;
    this.relativePath = relativePath;
    this.objectKey = objectKey;
    this.size = size;
    this.checksum = checksum;
    this.version = 1;
    this.status = FileStatus.PENDING;
    this.createdAt = now;
    this.updatedAt = now;
  }

  /** Tạo bản ghi file mới (phiên bản 1, trạng thái PENDING chờ upload nội dung). */
  public static SyncedFile register(
      UUID userId, String deviceId, String relativePath, String objectKey, long size,
      String checksum) {
    return new SyncedFile(userId, deviceId, relativePath, objectKey, size, checksum);
  }

  /** Đánh dấu đã khởi tạo phiên upload multipart, chờ client gửi các phần dữ liệu. */
  public void markInitiated() {
    this.status = FileStatus.INITIATED;
    this.updatedAt = Instant.now();
  }

  /** Đánh dấu đang truyền các phần dữ liệu (chuyển từ INITIATED sang UPLOADING). */
  public void markUploading() {
    this.status = FileStatus.UPLOADING;
    this.updatedAt = Instant.now();
  }

  /** Phiên còn nhận thêm phần dữ liệu (đã khởi tạo hoặc đang upload dở). */
  public boolean isAcceptingParts() {
    return this.status == FileStatus.INITIATED || this.status == FileStatus.UPLOADING;
  }

  /** Đánh dấu đã upload xong toàn bộ, đang đối chiếu checksum để xác thực toàn vẹn. */
  public void markVerifying() {
    this.status = FileStatus.VERIFYING;
    this.updatedAt = Instant.now();
  }

  /**
   * Hoàn tất upload thành công: ghi nhận kích thước/checksum thực tế và chuyển sang COMPLETED.
   */
  public void completeUpload(long finalSize, String finalChecksum) {
    this.size = finalSize;
    this.checksum = finalChecksum;
    this.status = FileStatus.COMPLETED;
    this.updatedAt = Instant.now();
  }

  /** Đánh dấu checksum sau khi upload không khớp — dữ liệu hỏng. */
  public void markChecksumMismatch() {
    this.status = FileStatus.CHECKSUM_MISMATCH;
    this.updatedAt = Instant.now();
  }

  /** Đánh dấu phiên upload bị huỷ bởi client hoặc do timeout. */
  public void markAborted() {
    this.status = FileStatus.ABORTED;
    this.updatedAt = Instant.now();
  }

  /** Trạng thái upload còn đang dở dang (chưa hoàn tất, chưa lỗi/huỷ) nên có thể huỷ. */
  public boolean isInProgress() {
    return this.status == FileStatus.INITIATED
        || this.status == FileStatus.UPLOADING
        || this.status == FileStatus.VERIFYING;
  }

  /** Đánh dấu nội dung đã upload xong và sẵn sàng tải về. */
  public void markAvailable() {
    this.status = FileStatus.AVAILABLE;
    this.updatedAt = Instant.now();
  }

  /**
   * Ghi nhận một phiên bản nội dung mới: tăng version, cập nhật kích thước/checksum/objectKey
   * và đưa trạng thái về PENDING chờ upload nội dung mới.
   */
  public void registerNewVersion(String newObjectKey, long newSize, String newChecksum) {
    this.version += 1;
    this.objectKey = newObjectKey;
    this.size = newSize;
    this.checksum = newChecksum;
    this.status = FileStatus.PENDING;
    this.updatedAt = Instant.now();
  }

  /** Đánh dấu có xung đột phiên bản cần giải quyết. */
  public void markConflicted() {
    this.status = FileStatus.CONFLICTED;
    this.updatedAt = Instant.now();
  }

  /** Xoá mềm bản ghi file. */
  public void markDeleted() {
    this.status = FileStatus.DELETED;
    this.updatedAt = Instant.now();
  }
}
