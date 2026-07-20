package org.example.file_sync_service.identity_context.domain.repository.credential;

import org.example.file_sync_service.identity_context.domain.model.entity.Credential;
import org.example.file_sync_service.identity_context.domain.model.enums.AuthProvider;
import java.util.Optional;

public interface ICredentialDomainRepository {
  void save(Credential credential);

  Optional<Credential> findByProviderAndProviderUserId(
      AuthProvider provider, String providerUserId);

  Optional<Credential> findByUserId(java.util.UUID userId);

  Optional<Credential> findByUserIdAndProvider(java.util.UUID userId, AuthProvider provider);
}
