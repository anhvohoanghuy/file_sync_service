package org.example.file_sync_service.file_context.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.file_sync_service.file_context.domain.model.aggregate.SyncedFile;
import org.example.file_sync_service.file_context.domain.model.enums.FileStatus;

/**
 * Cổng (port) domain cho việc lưu trữ {@link SyncedFile}. Tầng domain chỉ phụ thuộc interface này,
 * phần cài đặt nằm ở infrastructure.
 */
public interface ISyncedFileDomainRepository {

  Optional<SyncedFile> findById(UUID id);

  Optional<SyncedFile> findByObjectKey(String objectKey);

  Optional<SyncedFile> findByLocation(UUID userId, String deviceId, String relativePath);

  List<SyncedFile> findAllByUserIdAndDeviceId(UUID userId, String deviceId);

  List<SyncedFile> findAllByUserIdAndStatus(UUID userId, FileStatus status);

  SyncedFile save(SyncedFile file);

  void delete(SyncedFile file);
}
