package com.goatce.dto;

import jakarta.validation.constraints.NotBlank;

public record SaveContentRequest(
        @NotBlank String content
) {}
