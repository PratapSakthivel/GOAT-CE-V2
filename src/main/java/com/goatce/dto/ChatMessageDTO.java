package com.goatce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String messageId;
    private String roomCode;
    private String userId;
    private String userName;
    private String userColor;
    private String message;
    private LocalDateTime sentAt;
    private String type; // MESSAGE, USER_JOINED, USER_LEFT, SYSTEM
}
