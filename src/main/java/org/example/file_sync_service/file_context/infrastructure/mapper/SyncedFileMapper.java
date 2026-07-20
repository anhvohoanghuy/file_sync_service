package org.example.file_sync_service.file_context.infrastructure.mapper;

import java.util.Optional;
import org.example.file_sync_service.file_context.domain.model.aggregate.SyncedFile;
import org.example.file_sync_service.file_context.infrastructure.entity.SyncedFileEntity;

/**
 * Chuyển đổi hai chiều giữa aggregate {@link SyncedFile} và JPA entity {@link SyncedFileEntity}.
 */
public class SyncedFileMapper {

  private SyncedFileMapper() {}

  public static Optional<SyncedFile> toDomain(SyncedFileEntity entity) {
    if (entity == null) {
      return Optional.empty();
    }

    return Optional.of(
        new SyncedFile(
            entity.getId(),
            entity.getUserId(),
            entity.getDeviceId(),
            entity.getRelativePath(),
            entity.getObjectKey(),
            entity.getSize(),
            entity.getChecksum(),
            entity.getVersion(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()));
  }

  public static SyncedFileEntity toEntity(SyncedFile file) {
    if (file == null) {
      return null;
    }

    SyncedFileEntity entity = new SyncedFileEntity();
    entity.setId(file.getId());
    entity.setUserId(file.getUserId());
    entity.setDeviceId(file.getDeviceId());
    entity.setRelativePath(file.getRelativePath());
    entity.setObjectKey(file.getObjectKey());
    entity.setSize(file.getSize());
    entity.setChecksum(file.getChecksum());
    entity.setVersion(file.getVersion());
    entity.setStatus(file.getStatus());
    entity.setCreatedAt(file.getCreatedAt());
    entity.setUpdatedAt(file.getUpdatedAt());
    return entity;
  }
}
