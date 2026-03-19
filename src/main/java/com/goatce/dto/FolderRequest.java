package com.goatce.dto;

import jakarta.validation.constraints.NotBlank;

public record FolderRequest(
        @NotBlank String name
) {}
