package org.example.file_sync_service.file_context.domain.model.enums;

/**
 * Trạng thái vòng đời của một file được đồng bộ.
 */
public enum FileStatus {
  /** Đã tạo bản ghi metadata nhưng nội dung chưa upload xong lên MinIO. */
  PENDING,
  /** Nội dung đã sẵn sàng trên object storage, có thể tải về. */
  AVAILABLE,
  /** Có xung đột phiên bản giữa các thiết bị, cần giải quyết. */
  CONFLICTED,
  /** Đã bị xoá (soft-delete) — object có thể vẫn được giữ để khôi phục. */
  DELETED
}
