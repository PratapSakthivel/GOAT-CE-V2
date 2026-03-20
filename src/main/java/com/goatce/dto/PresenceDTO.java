package com.goatce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceDTO {
    private String roomCode;
    private String userId;
    private String userName;
    private String userColor;
    private String status; // JOINED, LEFT, ACTIVE
}
