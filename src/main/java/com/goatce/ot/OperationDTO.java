package com.goatce.ot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationDTO {
    private String operationId;
    private String roomCode;
    private String userId;
    private String userName;
    private String userColor;
    private String type; // INSERT or DELETE
    private int position;
    private String character; // null for DELETE
    private int revision;
}
