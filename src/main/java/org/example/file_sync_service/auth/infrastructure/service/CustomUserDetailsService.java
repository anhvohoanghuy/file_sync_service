package org.example.file_sync_service.auth.infrastructure.service;

import org.example.file_sync_service.auth.infrastructure.security.CustomUserDetails;
import org.example.file_sync_service.identity_context.domain.model.aggregate.User;
import org.example.file_sync_service.identity_context.domain.model.entity.Credential;
import org.example.file_sync_service.identity_context.domain.model.entity.Role;
import org.example.file_sync_service.identity_context.domain.repository.credential.ICredentialDomainRepository;
import org.example.file_sync_service.identity_context.domain.repository.user.IUserDomainRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {
  private final IUserDomainRepository userDomainRepository;
  private final ICredentialDomainRepository credentialDomainRepository;

  public CustomUserDetails loadUserById(UUID id) {
    Optional<User> userDomain = userDomainRepository.findByIdWithRoles(id);
    Optional<Credential> credentialDomain = credentialDomainRepository.findByUserId(id);
    if (userDomain.isEmpty() || credentialDomain.isEmpty()) {
      throw new RuntimeException("User not found");
    }
    return CustomUserDetails.builder()
        .id(userDomain.get().getId())
        .email(userDomain.get().getEmail())
        .roles(userDomain.get().getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
        .password(
            credentialDomain.get().getPasswordHash() == null
                ? ""
                : credentialDomain.get().getPasswordHash())
        .build();
  }
}
