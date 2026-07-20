package org.example.file_sync_service.identity_context.application.usecase;

import org.example.file_sync_service.identity_context.application.dto.RegisterRequestDto;
import org.example.file_sync_service.identity_context.application.dto.RoleEnum;
import org.example.file_sync_service.identity_context.domain.model.aggregate.User;
import org.example.file_sync_service.identity_context.domain.model.entity.Credential;
import org.example.file_sync_service.identity_context.domain.model.entity.Role;
import org.example.file_sync_service.identity_context.domain.repository.credential.ICredentialDomainRepository;
import org.example.file_sync_service.identity_context.domain.repository.role.IRoleDomainRepository;
import org.example.file_sync_service.identity_context.domain.repository.user.IUserDomainRepository;
import org.example.file_sync_service.identity_context.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterUserUseCase {
  private final UserDomainService userDomainService;
  private final IUserDomainRepository userDomainRepository;
  private final IRoleDomainRepository roleDomainRepository;
  private final PasswordEncoder passwordEncoder;
  private final ICredentialDomainRepository credentialDomainRepository;

  public void execute(RegisterRequestDto requestDto) {
    User user = User.register(requestDto.getUsername(), requestDto.getEmail());
    userDomainService.validateUser(user);

    Role role =
        roleDomainRepository
            .findByName(RoleEnum.USER.getName())
            .orElseThrow(
                () -> new IllegalStateException("Role seed missing: " + RoleEnum.USER.getName()));
    user.assignRole(role);
    userDomainRepository.save(user);

    Credential credential =
        Credential.create(
            user.getId(),
            requestDto.getLoginType(),
            requestDto.getProviderUserId(),
            passwordEncoder.encode(requestDto.getPassword()));
    credentialDomainRepository.save(credential);
  }
}
