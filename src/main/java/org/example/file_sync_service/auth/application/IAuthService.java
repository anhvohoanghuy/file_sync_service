package org.example.file_sync_service.auth.application;

import org.example.file_sync_service.auth.application.dto.AuthRequest;
import org.example.file_sync_service.auth.application.dto.AuthRequestMetadata;
import org.example.file_sync_service.auth.application.dto.AuthResponse;

public interface IAuthService {
  public AuthResponse login(AuthRequest authRequest);

  public AuthResponse refreshToken(String refreshToken);

  public AuthResponse refreshToken(String refreshToken, AuthRequestMetadata metadata);

  public void logout(String refreshToken);
}
