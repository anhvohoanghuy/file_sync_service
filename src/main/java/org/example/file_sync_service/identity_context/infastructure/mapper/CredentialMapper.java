package org.example.file_sync_service.identity_context.infastructure.mapper;

import org.example.file_sync_service.identity_context.domain.model.entity.Credential;
import org.example.file_sync_service.identity_context.domain.model.enums.AuthProvider;
import org.example.file_sync_service.identity_context.infastructure.entity.CredentialEntity;

public class CredentialMapper {
  public static Credential toDomain(CredentialEntity credentialEntity) {
    if (credentialEntity == null) {
      return null;
    }
    return new Credential(
        credentialEntity.getId(),
        credentialEntity.getUserId(),
        credentialEntity.getProviderUserId(),
        credentialEntity.getPasswordHash(),
        AuthProvider.fromString(credentialEntity.getAuthProvider()));
  }

  public static CredentialEntity toEntity(Credential credential) {
    if (credential == null) {
      return null;
    }
    return new CredentialEntity(
        credential.getId(),
        credential.getUserId(),
        credential.getProviderUserId(),
        credential.getPasswordHash(),
        credential.getAuthProvider().toString());
  }
}
