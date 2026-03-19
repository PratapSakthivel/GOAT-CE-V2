package com.goatce.ot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorDTO {
    private String roomCode;
    private String userId;
    private String userName;
    private String userColor;
    private int position;
    private int line;
    private int column;
}
