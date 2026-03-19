package com.goatce.ot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingDTO {
    private String roomCode;
    private String userId;
    private String userName;
    private boolean isTyping;
}
