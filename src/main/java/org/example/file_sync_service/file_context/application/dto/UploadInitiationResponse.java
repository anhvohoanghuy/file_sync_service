package org.example.file_sync_service.file_context.application.dto;

import java.util.UUID;
import org.example.file_sync_service.file_context.domain.model.enums.FileStatus;

/**
 * Kết quả trả về khi client yêu cầu khởi tạo một phiên upload multipart.
 *
 * @param fileUploadId định danh phiên upload (chính là id của bản ghi {@code SyncedFile}); client
 *     dùng giá trị này để gửi từng phần dữ liệu và hoàn tất upload.
 * @param objectKey khoá object trên MinIO nơi nội dung sẽ được lưu.
 * @param bucket bucket chứa object.
 * @param status trạng thái hiện tại của phiên upload (INITIATED).
 */
public record UploadInitiationResponse(
    UUID fileUploadId, String objectKey, String bucket, FileStatus status) {}
