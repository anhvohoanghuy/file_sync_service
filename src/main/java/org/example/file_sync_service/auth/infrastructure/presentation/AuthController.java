package org.example.file_sync_service.auth.infrastructure.presentation;

import org.example.file_sync_service.auth.application.AuthService;
import org.example.file_sync_service.auth.application.dto.AuthRequest;
import org.example.file_sync_service.auth.application.dto.AuthRequestMetadata;
import org.example.file_sync_service.auth.application.dto.AuthResponse;
import org.example.file_sync_service.auth.application.dto.LoginRequest;
import org.example.file_sync_service.auth.application.dto.RefreshTokenRequest;
import org.example.file_sync_service.auth.application.dto.RegisterLocalRequest;
import org.example.file_sync_service.auth.domain.model.AuthType;
import org.example.file_sync_service.common.exception.AppException;
import org.example.file_sync_service.identity_context.application.dto.RegisterRequestDto;
import org.example.file_sync_service.identity_context.application.dto.RoleEnum;
import org.example.file_sync_service.identity_context.application.usecase.RegisterUserUseCase;
import org.example.file_sync_service.identity_context.domain.model.enums.AuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints của Auth context: đăng ký, đăng nhập, làm mới token, đăng xuất.
 * Lát cắt tối giản (local + JWT access/refresh) port từ dự án feat1.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final RegisterUserUseCase registerUserUseCase;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @RequestBody RegisterLocalRequest request, HttpServletRequest servletRequest) {
    AuthRequestMetadata metadata = metadata(servletRequest);

    RegisterRequestDto registerRequest = new RegisterRequestDto();
    registerRequest.setUsername(request.username());
    registerRequest.setEmail(request.email());
    registerRequest.setPassword(request.password());
    registerRequest.setProviderUserId(request.username());
    registerRequest.setLoginType(AuthProvider.LOCAL);
    registerRequest.setRoles(List.of(RoleEnum.USER.getName()));

    registerUserUseCase.execute(registerRequest);

    AuthResponse response =
        authService.login(toLocalAuthRequest(request.username(), request.password(), metadata));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
    AuthRequestMetadata metadata = metadata(servletRequest);
    String username = request == null ? null : request.username();
    String password = request == null ? null : request.password();
    AuthResponse response = authService.login(toLocalAuthRequest(username, password, metadata));
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(
      @RequestBody RefreshTokenRequest request, HttpServletRequest servletRequest) {
    AuthRequestMetadata metadata = metadata(servletRequest);
    AuthResponse response = authService.refreshToken(requiredRefreshToken(request), metadata);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
    authService.logout(requiredRefreshToken(request));
    return ResponseEntity.noContent().build();
  }

  private AuthRequest toLocalAuthRequest(
      String username, String password, AuthRequestMetadata metadata) {
    return new AuthRequest(AuthType.LOCAL, username, password, null, metadata);
  }

  private String requiredRefreshToken(RefreshTokenRequest request) {
    if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
      throw new AppException(
          "REFRESH_TOKEN_REQUIRED", "Refresh token is required", HttpStatus.BAD_REQUEST);
    }
    return request.refreshToken();
  }

  private AuthRequestMetadata metadata(HttpServletRequest request) {
    if (request == null) {
      return AuthRequestMetadata.empty();
    }
    return new AuthRequestMetadata(clientIp(request), request.getHeader(HttpHeaders.USER_AGENT));
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
