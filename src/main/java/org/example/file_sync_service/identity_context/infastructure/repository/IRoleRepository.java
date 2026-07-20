package org.example.file_sync_service.identity_context.infastructure.repository;

import org.example.file_sync_service.identity_context.infastructure.entity.RoleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoleRepository extends JpaRepository<RoleEntity, UUID> {
  Optional<RoleEntity> findByName(String name);
}
