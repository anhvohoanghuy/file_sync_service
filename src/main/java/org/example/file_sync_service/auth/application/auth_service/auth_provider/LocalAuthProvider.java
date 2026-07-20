package org.example.file_sync_service.auth.application.auth_service.auth_provider;

import org.example.file_sync_service.auth.TokenSerivce;
import org.example.file_sync_service.auth.application.dto.AuthRequest;
import org.example.file_sync_service.auth.application.dto.AuthResponse;
import org.example.file_sync_service.identity_context.domain.model.aggregate.User;
import org.example.file_sync_service.identity_context.domain.model.entity.Credential;
import org.example.file_sync_service.identity_context.domain.model.enums.AuthProvider;
import org.example.file_sync_service.identity_context.domain.repository.credential.ICredentialDomainRepository;
import org.example.file_sync_service.identity_context.domain.repository.user.IUserDomainRepository;
import org.example.file_sync_service.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component("LOCAL")
@RequiredArgsConstructor
public class LocalAuthProvider implements IAuthProvider {
  private final ICredentialDomainRepository credentialDomainRepository;
  private final IUserDomainRepository userDomainRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenSerivce tokenSerivce;

  @Override
  public AuthResponse authenticate(AuthRequest authRequest) {
    Credential credential =
        credentialDomainRepository
            .findByProviderAndProviderUserId(AuthProvider.LOCAL, authRequest.getUsername())
            .orElseThrow(() -> invalidCredentials());

    if (!passwordEncoder.matches(authRequest.getPassword(), credential.getPasswordHash())) {
      throw invalidCredentials();
    }

    User user =
        userDomainRepository
            .findByIdWithRoles(credential.getUserId())
            .orElseThrow(
                () ->
                    new AppException("USER_NOT_FOUND", "User not found", HttpStatus.UNAUTHORIZED));

    return tokenSerivce.generateAccessToken(user, authRequest.getMetadata());
  }

  private AppException invalidCredentials() {
    return new AppException(
        "INVALID_CREDENTIALS", "Invalid username or password", HttpStatus.UNAUTHORIZED);
  }
}
