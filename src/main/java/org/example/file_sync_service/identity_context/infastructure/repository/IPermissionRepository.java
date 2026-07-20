package org.example.file_sync_service.identity_context.infastructure.repository;

import org.example.file_sync_service.identity_context.infastructure.entity.PermissionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPermissionRepository extends JpaRepository<PermissionEntity, UUID> {
  Optional<PermissionEntity> findByCode(String code);
}
