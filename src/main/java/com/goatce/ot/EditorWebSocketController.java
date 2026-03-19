package com.goatce.ot;

import com.goatce.model.CollabRoom;
import com.goatce.model.Operation;
import com.goatce.repository.CollabRoomRepository;
import com.goatce.repository.OperationRepository;
import com.goatce.repository.RoomParticipantRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class EditorWebSocketController {

    private final OperationalTransformService otService;
    private final CollabRoomRepository collabRoomRepository;
    private final OperationRepository operationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomParticipantRepository roomParticipantRepository;

    public EditorWebSocketController(OperationalTransformService otService,
                                     CollabRoomRepository collabRoomRepository,
                                     OperationRepository operationRepository,
                                     SimpMessagingTemplate messagingTemplate,
                                     RoomParticipantRepository roomParticipantRepository) {
        this.otService = otService;
        this.collabRoomRepository = collabRoomRepository;
        this.operationRepository = operationRepository;
        this.messagingTemplate = messagingTemplate;
        this.roomParticipantRepository = roomParticipantRepository;
    }

    @MessageMapping("/editor/{roomCode}/operation")
    @Transactional
    public void handleOperation(@DestinationVariable String roomCode,
                                @Payload OperationDTO incomingOp,
                                Principal principal) {
        
        CollabRoom room = collabRoomRepository.findByRoomCodeForUpdate(roomCode).orElse(null);
        if (room == null) return;

        List<Operation> concurrentOps = operationRepository.findByRoomIdAndRevisionGreaterThan(
                room.getId().toString(), incomingOp.getRevision());

        for (Operation concurrentOp : concurrentOps) {
            OperationDTO serverOp = OperationDTO.builder()
                    .type(concurrentOp.getType())
                    .position(concurrentOp.getPosition())
                    .character(concurrentOp.getCharacter())
                    .build();

            OperationDTO transformed = otService.transform(serverOp, incomingOp);
            if (transformed == null) return; // no-op
            
            incomingOp = transformed;
        }

        String newContent = otService.applyOperation(room.getContent(), incomingOp);
        room.setContent(newContent);
        
        int newRevision = room.getRevision() + 1;
        room.setRevision(newRevision);
        collabRoomRepository.save(room);

        Operation newOp = Operation.builder()
                .roomId(room.getId().toString())
                .userId(incomingOp.getUserId())
                .userName(incomingOp.getUserName())
                .type(incomingOp.getType())
                .position(incomingOp.getPosition())
                .character(incomingOp.getCharacter())
                .revision(newRevision)
                .build();
        operationRepository.save(newOp);

        SyncResponseDTO syncResponse = SyncResponseDTO.builder()
                .operationId(incomingOp.getOperationId())
                .type(incomingOp.getType())
                .position(incomingOp.getPosition())
                .character(incomingOp.getCharacter())
                .revision(newRevision)
                .userId(incomingOp.getUserId())
                .userName(incomingOp.getUserName())
                .userColor(incomingOp.getUserColor())
                .build();

        messagingTemplate.convertAndSend("/topic/editor/" + roomCode, syncResponse);
    }

    @MessageMapping("/editor/{roomCode}/cursor")
    public void handleCursor(@DestinationVariable String roomCode, @Payload CursorDTO cursorDTO) {
        messagingTemplate.convertAndSend("/topic/cursor/" + roomCode, cursorDTO);
    }

    @MessageMapping("/editor/{roomCode}/typing")
    public void handleTyping(@DestinationVariable String roomCode, @Payload TypingDTO typingDTO) {
        messagingTemplate.convertAndSend("/topic/typing/" + roomCode, typingDTO);
    }

    @MessageMapping("/editor/{roomCode}/sync")
    public void handleSyncRequest(@DestinationVariable String roomCode, Principal principal) {
        CollabRoom room = collabRoomRepository.findByRoomCode(roomCode).orElse(null);
        if (room != null) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/sync",
                    Map.of(
                            "content", room.getContent(),
                            "revision", room.getRevision()
                    )
            );
        }
    }
}
