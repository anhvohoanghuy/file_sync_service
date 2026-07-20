package org.example.file_sync_service.auth.application.auth_service.auth_provider;

import org.example.file_sync_service.auth.application.dto.AuthRequest;
import org.example.file_sync_service.auth.application.dto.AuthResponse;

public interface IAuthProvider {
  AuthResponse authenticate(AuthRequest authRequest);
}
