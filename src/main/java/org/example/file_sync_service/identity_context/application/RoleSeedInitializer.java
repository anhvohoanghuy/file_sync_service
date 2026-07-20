package org.example.file_sync_service.identity_context.application;

import org.example.file_sync_service.identity_context.application.dto.RoleEnum;
import org.example.file_sync_service.identity_context.infastructure.entity.RoleEntity;
import org.example.file_sync_service.identity_context.infastructure.repository.IRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeedInitializer implements CommandLineRunner {
  private final IRoleRepository roleRepository;

  @Override
  public void run(String... args) {
    seed(RoleEnum.USER);
    seed(RoleEnum.ADMIN);
    seed(RoleEnum.STAFF);
  }

  private void seed(RoleEnum role) {
    roleRepository
        .findByName(role.getName())
        .orElseGet(
            () -> {
              RoleEntity entity = new RoleEntity();
              entity.setId(role.getId());
              entity.setName(role.getName());
              return roleRepository.save(entity);
            });
  }
}
