package com.goatce.dto;

import jakarta.validation.constraints.NotBlank;

public record AiRequest(
        @NotBlank String prompt,
        String code,
        String language,
        String context
) {
    public AiRequest {
        if (context == null || context.isBlank()) {
            context = "general";
        }
    }
}
