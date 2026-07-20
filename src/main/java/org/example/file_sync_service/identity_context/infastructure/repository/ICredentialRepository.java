package org.example.file_sync_service.identity_context.infastructure.repository;

import org.example.file_sync_service.identity_context.infastructure.entity.CredentialEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICredentialRepository extends JpaRepository<CredentialEntity, UUID> {
  Optional<CredentialEntity> findByAuthProviderAndProviderUserId(
      String provider, String providerUserId);

  Optional<CredentialEntity> findByUserId(UUID userId);

  Optional<CredentialEntity> findByUserIdAndAuthProvider(UUID userId, String authProvider);
}
