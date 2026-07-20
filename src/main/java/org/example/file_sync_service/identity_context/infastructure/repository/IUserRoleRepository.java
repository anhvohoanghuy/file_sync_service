package org.example.file_sync_service.identity_context.infastructure.repository;

import org.example.file_sync_service.identity_context.infastructure.entity.UserRoleEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRoleRepository extends JpaRepository<UserRoleEntity, UUID> {
  List<UserRoleEntity> findByUser_Id(UUID userId);
}
