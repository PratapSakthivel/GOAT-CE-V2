package com.goatce.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleChangeRequest(
    @NotBlank String targetUserId,
    @NotBlank String newRole // EDITOR or VIEWER
) {}
