package com.goatce.dto;

public record AuthResponse(
        String token,
        String name,
        String email
) {}
