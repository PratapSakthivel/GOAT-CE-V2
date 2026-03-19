package com.goatce.dto;

import jakarta.validation.constraints.NotBlank;

public record FileRequest(
        @NotBlank String name,
        @NotBlank String language,
        String content
) {}
