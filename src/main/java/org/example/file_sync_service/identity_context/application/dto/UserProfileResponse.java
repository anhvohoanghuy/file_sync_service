package org.example.file_sync_service.identity_context.application.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String name,
    String email,
    boolean emailVerified,
    Instant emailVerifiedAt,
    Set<String> roles) {}
