package org.example.file_sync_service.file_context.presentation;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.file_sync_service.auth.infrastructure.security.CustomUserDetails;
import org.example.file_sync_service.common.exception.AppException;
import org.example.file_sync_service.file_context.application.dto.CompleteUploadRequest;
import org.example.file_sync_service.file_context.application.dto.UploadCompletionResponse;
import org.example.file_sync_service.file_context.application.dto.UploadInitiationResponse;
import org.example.file_sync_service.file_context.application.dto.UploadPartResponse;
import org.example.file_sync_service.file_context.application.dto.UploadRequest;
import org.example.file_sync_service.file_context.application.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Điểm vào HTTP cho luồng upload multipart của {@link FileService}. Controller thuộc tầng
 * presentation của bounded context {@code file_context}: chỉ lấy ngữ cảnh xác thực, chuyển đổi
 * request HTTP sang lời gọi application service và trả về DTO; không chứa logic nghiệp vụ.
 */
@RestController
@RequestMapping("/files/uploads")
@RequiredArgsConstructor
public class FileController {

  private final FileService fileService;

  @PostMapping
  public UploadInitiationResponse initiate(
      @AuthenticationPrincipal CustomUserDetails principal,
      @RequestHeader("X-Device-Id") String deviceId,
      @RequestBody UploadRequest request) {
    return fileService.initiateMultipartUpload(requireUserId(principal), deviceId, request);
  }

  @PutMapping(
      path = "/{fileUploadId}/parts/{partNumber}",
      consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public UploadPartResponse uploadPart(
      @AuthenticationPrincipal CustomUserDetails principal,
      @PathVariable UUID fileUploadId,
      @PathVariable int partNumber,
      HttpServletRequest request) {
    long size = request.getContentLengthLong();
    if (size <= 0) {
      throw new AppException(
          "CONTENT_LENGTH_REQUIRED",
          "A positive Content-Length is required for the part body",
          HttpStatus.BAD_REQUEST);
    }

    try (InputStream data = request.getInputStream()) {
      return fileService.uploadPart(
          requireUserId(principal), fileUploadId, partNumber, data, size);
    } catch (IOException e) {
      throw new AppException(
          "PART_READ_FAILED", "Failed to read part data", HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/{fileUploadId}/complete")
  public UploadCompletionResponse complete(
      @AuthenticationPrincipal CustomUserDetails principal,
      @PathVariable UUID fileUploadId,
      @RequestBody CompleteUploadRequest request) {
    return fileService.completeMultipartUpload(requireUserId(principal), fileUploadId, request);
  }

  @PostMapping("/{fileUploadId}/abort")
  public ResponseEntity<Void> abort(
      @AuthenticationPrincipal CustomUserDetails principal,
      @PathVariable UUID fileUploadId) {
    fileService.abortMultipartUpload(requireUserId(principal), fileUploadId);
    return ResponseEntity.noContent().build();
  }

  private UUID requireUserId(CustomUserDetails principal) {
    if (principal == null) {
      throw new AppException(
          "UNAUTHENTICATED", "Authentication is required", HttpStatus.UNAUTHORIZED);
    }
    return principal.getId();
  }
}
