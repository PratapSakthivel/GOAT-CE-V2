package com.goatce.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FolderResponse(
        UUID id,
        String name,
        LocalDateTime createdAt
) {}
