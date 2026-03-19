package com.goatce.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRoomRequest(
        @NotBlank String name,
        @NotBlank String language
) {}
