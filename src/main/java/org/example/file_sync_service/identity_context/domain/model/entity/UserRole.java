package org.example.file_sync_service.identity_context.domain.model.entity;

import org.example.file_sync_service.identity_context.domain.model.aggregate.User;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {
  private UUID id;
  private User user;
  private Role role;

  public UserRole(User user, Role role) {
    this.id = UUID.randomUUID();
    this.user = user;
    this.role = role;
  }
}
