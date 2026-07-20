package org.example.file_sync_service.file_context.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.file_sync_service.file_context.domain.model.aggregate.SyncedFile;
import org.example.file_sync_service.file_context.domain.model.enums.FileStatus;
import org.example.file_sync_service.file_context.infrastructure.entity.SyncedFileEntity;
import org.example.file_sync_service.file_context.infrastructure.mapper.SyncedFileMapper;
import org.example.file_sync_service.file_context.infrastructure.repository.ISyncedFileRepository;
import org.springframework.stereotype.Repository;

/** Cài đặt {@link ISyncedFileDomainRepository} bằng cách uỷ quyền cho Spring Data JPA + mapper. */
@Repository
@RequiredArgsConstructor
public class SyncedFileDomainRepository implements ISyncedFileDomainRepository {

  private final ISyncedFileRepository jpaRepo;

  @Override
  public Optional<SyncedFile> findById(UUID id) {
    return SyncedFileMapper.toDomain(jpaRepo.findById(id).orElse(null));
  }

  @Override
  public Optional<SyncedFile> findByObjectKey(String objectKey) {
    return SyncedFileMapper.toDomain(jpaRepo.findByObjectKey(objectKey).orElse(null));
  }

  @Override
  public Optional<SyncedFile> findByLocation(UUID userId, String deviceId, String relativePath) {
    return SyncedFileMapper.toDomain(
        jpaRepo
            .findByUserIdAndDeviceIdAndRelativePath(userId, deviceId, relativePath)
            .orElse(null));
  }

  @Override
  public List<SyncedFile> findAllByUserIdAndDeviceId(UUID userId, String deviceId) {
    return jpaRepo.findAllByUserIdAndDeviceId(userId, deviceId).stream()
        .map(SyncedFileMapper::toDomain)
        .flatMap(Optional::stream)
        .toList();
  }

  @Override
  public List<SyncedFile> findAllByUserIdAndStatus(UUID userId, FileStatus status) {
    return jpaRepo.findAllByUserIdAndStatus(userId, status).stream()
        .map(SyncedFileMapper::toDomain)
        .flatMap(Optional::stream)
        .toList();
  }

  @Override
  public SyncedFile save(SyncedFile file) {
    SyncedFileEntity saved = jpaRepo.save(SyncedFileMapper.toEntity(file));
    return SyncedFileMapper.toDomain(saved).orElseThrow();
  }

  @Override
  public void delete(SyncedFile file) {
    jpaRepo.delete(SyncedFileMapper.toEntity(file));
  }
}
