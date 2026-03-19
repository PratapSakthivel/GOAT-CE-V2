package com.goatce.dto;

import java.util.List;
import java.util.UUID;

public record JoinRoomResponse(
        UUID roomId,
        String roomCode,
        String name,
        String language,
        String content,
        Integer revision,
        String yourRole,
        List<ParticipantInfo> participants
) {}
