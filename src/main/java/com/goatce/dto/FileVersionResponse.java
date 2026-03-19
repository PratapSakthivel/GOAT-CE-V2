package com.goatce.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileVersionResponse(
        UUID id,
        Integer versionNumber,
        String content,
        LocalDateTime savedAt
) {}
