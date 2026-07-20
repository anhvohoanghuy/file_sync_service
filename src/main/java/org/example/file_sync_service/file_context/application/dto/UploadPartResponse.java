package org.example.file_sync_service.file_context.application.dto;

import java.util.UUID;
import org.example.file_sync_service.file_context.domain.model.enums.FileStatus;

/**
 * Kết quả trả về sau khi client gửi thành công một part của phiên upload multipart.
 *
 * @param fileUploadId định danh phiên upload.
 * @param partNumber số thứ tự của part vừa nhận.
 * @param etag ETag của part (client giữ lại để hoàn tất upload sau này).
 * @param status trạng thái hiện tại của phiên upload (UPLOADING).
 */
public record UploadPartResponse(
    UUID fileUploadId, int partNumber, String etag, FileStatus status) {}
