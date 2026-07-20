package org.example.file_sync_service.identity_context.infastructure.mapper;

import org.example.file_sync_service.identity_context.domain.model.aggregate.User;
import org.example.file_sync_service.identity_context.domain.repository.user.IUserDomainRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final IUserDomainRepository userRepository;

  public Optional<User> getUserById(UUID id) {
    return userRepository.findByIdWithRoles(id);
  }

  public void createUser() {}
}
