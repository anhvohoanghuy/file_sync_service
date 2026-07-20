package org.example.file_sync_service.identity_context.domain.repository.role;

import org.example.file_sync_service.identity_context.domain.model.entity.Role;
import java.util.Optional;
import java.util.UUID;

public interface IRoleDomainRepository {
  void save(String name);

  void delete(String name);

  Optional<Role> findByName(String name);

  Role findReferenceById(UUID id);
}
