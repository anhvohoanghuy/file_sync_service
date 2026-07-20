package org.example.file_sync_service.identity_context.domain.model.entity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
  private UUID id;
  private String code;

  public Permission create(String code) {
    return new Permission(UUID.randomUUID(), code);
  }
}
