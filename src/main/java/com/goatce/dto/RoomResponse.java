package com.goatce.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        String name,
        String language,
        String roomCode,
        String ownerName,
        String content,
        Integer revision,
        LocalDateTime createdAt
) {}
