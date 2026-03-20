package com.goatce.dto;

public record AiResponse(
        String response,
        boolean success,
        String error
) {}
