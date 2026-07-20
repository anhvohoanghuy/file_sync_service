package org.example.file_sync_service.auth.application.dto;

public record AuthRequestMetadata(String ipAddress, String userAgent) {
  public static AuthRequestMetadata empty() {
    return new AuthRequestMetadata(null, null);
  }
}
