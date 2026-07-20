package org.example.file_sync_service.file_context.application.dto;

import java.util.List;

/**
 * Yêu cầu hoàn tất một phiên upload multipart.
 *
 * @param partNumbers danh sách số thứ tự part theo đúng thứ tự cần ghép.
 * @param expectedChecksum checksum mong đợi của toàn bộ file (dạng {@code "sha256:<hex>"}); có thể
 *     null nếu client không gửi, khi đó server chỉ tính và lưu checksum thực tế.
 */
public record CompleteUploadRequest(List<Integer> partNumbers, String expectedChecksum) {}
