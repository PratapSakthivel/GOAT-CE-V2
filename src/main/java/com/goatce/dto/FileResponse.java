package com.goatce.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileResponse(
        UUID id,
        String name,
        String language,
        String content,
        UUID folderId,
        String folderName,
        boolean isPublic,
        String publicToken,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
