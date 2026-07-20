package org.example.file_sync_service.file_context.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.file_sync_service.file_context.domain.model.enums.FileStatus;

/**
 * Bảng metadata file được đồng bộ. Nội dung thật nằm trên MinIO (theo {@code objectKey}),
 * bảng này chỉ lưu thông tin mô tả.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(
    name = "synced_files",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_synced_files_owner_path",
          columnNames = {"user_id", "device_id", "relative_path"}),
      @UniqueConstraint(name = "uk_synced_files_object_key", columnNames = "object_key")
    },
    indexes = {
      @Index(name = "idx_synced_files_user_device", columnList = "user_id, device_id"),
      @Index(name = "idx_synced_files_status", columnList = "status")
    })
public class SyncedFileEntity {

  @Id @ToString.Include private UUID id;

  @Column(name = "user_id", nullable = false)
  @ToString.Include
  private UUID userId;

  @Column(name = "device_id", nullable = false, length = 128)
  @ToString.Include
  private String deviceId;

  @Column(name = "relative_path", nullable = false, length = 1024)
  @ToString.Include
  private String relativePath;

  @Column(name = "object_key", nullable = false, length = 1024)
  private String objectKey;

  @Column(name = "size", nullable = false)
  private long size;

  @Column(name = "checksum", length = 255)
  private String checksum;

  @Column(name = "version", nullable = false)
  @ToString.Include
  private int version;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  @ToString.Include
  private FileStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
