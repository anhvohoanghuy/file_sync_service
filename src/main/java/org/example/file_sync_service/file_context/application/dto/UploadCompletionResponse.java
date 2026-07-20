package org.example.file_sync_service.file_context.application.dto;

import java.util.UUID;
import org.example.file_sync_service.file_context.domain.model.enums.FileStatus;

/**
 * Kết quả trả về sau khi hoàn tất phiên upload multipart.
 *
 * @param fileUploadId định danh phiên upload.
 * @param objectKey khoá object cuối cùng trên MinIO.
 * @param size kích thước file sau khi ghép (byte).
 * @param checksum checksum thực tế của file (dạng {@code "sha256:<hex>"}).
 * @param status trạng thái cuối cùng (COMPLETED nếu thành công).
 */
public record UploadCompletionResponse(
    UUID fileUploadId, String objectKey, long size, String checksum, FileStatus status) {}
