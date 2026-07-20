package org.example.file_sync_service.identity_context.application.dto;

import org.example.file_sync_service.identity_context.domain.model.enums.AuthProvider;
import java.util.List;
import lombok.Data;

@Data
public class RegisterRequestDto {
  private String username;
  private String email;
  private String password;
  private String providerUserId;
  private AuthProvider loginType;
  private List<String> roles;
}
