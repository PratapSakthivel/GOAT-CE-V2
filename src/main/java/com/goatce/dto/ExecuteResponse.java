package com.goatce.dto;

public record ExecuteResponse(
        String output,
        String error,
        int exitCode,
        boolean success,
        String language,
        long executionTimeMs
) {}
