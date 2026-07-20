package org.example.file_sync_service.file_context.domain.model.enums;

/**
 * Trạng thái vòng đời của một file được đồng bộ.
 */
public enum FileStatus {
  /** Đã tạo bản ghi metadata nhưng nội dung chưa upload xong lên MinIO. */
  PENDING,

  /** Phiên upload vừa được khởi tạo (đã cấp upload id / pre-signed url) nhưng chưa gửi dữ liệu. */
  INITIATED,
  /** Đang truyền các phần dữ liệu (chunk/multipart) lên object storage. */
  UPLOADING,
  /** Đã upload xong, đang đối chiếu checksum để xác thực toàn vẹn. */
  VERIFYING,
  /** Upload đã hoàn tất và được xác thực thành công. */
  COMPLETED,

  /** Nội dung đã sẵn sàng trên object storage, có thể tải về. */
  AVAILABLE,
  /** Có xung đột phiên bản giữa các thiết bị, cần giải quyết. */
  CONFLICTED,

  /** Upload thất bại vì lỗi (mạng, storage, ...) — có thể thử lại. */
  FAILED,
  /** Phiên upload bị huỷ bởi client hoặc do timeout. */
  ABORTED,
  /** Checksum sau khi upload không khớp với giá trị mong đợi — dữ liệu hỏng. */
  CHECKSUM_MISMATCH,

  /** Đã bị xoá (soft-delete) — object có thể vẫn được giữ để khôi phục. */
  DELETED
}
