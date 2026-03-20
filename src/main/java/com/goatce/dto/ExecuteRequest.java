package com.goatce.dto;

import jakarta.validation.constraints.NotBlank;

public record ExecuteRequest(
        @NotBlank String language,
        @NotBlank String code,
        String stdin
) {}
