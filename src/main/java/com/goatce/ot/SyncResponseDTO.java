package com.goatce.ot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponseDTO {
    private String operationId;
    private String type; // INSERT or DELETE
    private int position;
    private String character;
    private int revision;
    private String userId;
    private String userName;
    private String userColor;
}
