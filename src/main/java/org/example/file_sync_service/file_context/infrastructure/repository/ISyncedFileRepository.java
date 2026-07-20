package org.example.file_sync_service.file_context.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.file_sync_service.file_context.domain.model.enums.FileStatus;
import org.example.file_sync_service.file_context.infrastructure.entity.SyncedFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository cho {@link SyncedFileEntity}. */
public interface ISyncedFileRepository extends JpaRepository<SyncedFileEntity, UUID> {

  Optional<SyncedFileEntity> findByObjectKey(String objectKey);

  Optional<SyncedFileEntity> findByUserIdAndDeviceIdAndRelativePath(
      UUID userId, String deviceId, String relativePath);

  List<SyncedFileEntity> findAllByUserIdAndDeviceId(UUID userId, String deviceId);

  List<SyncedFileEntity> findAllByUserIdAndStatus(UUID userId, FileStatus status);
}
