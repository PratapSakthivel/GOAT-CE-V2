package com.goatce.dto;

import java.util.UUID;

public record ParticipantInfo(
        UUID userId,
        String name,
        String email,
        String role,
        String cursorColor
) {}
