package org.example.file_sync_service.identity_context.domain.model.entity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class rolePermision {
  private UUID id;
  private Role role;
  private Permission permission;
}
