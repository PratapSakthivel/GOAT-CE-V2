package com.goatce.ot;

import com.goatce.dto.ChatMessageDTO;
import com.goatce.dto.PresenceDTO;
import com.goatce.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{roomCode}/send")
    public void handleChatMessage(@DestinationVariable String roomCode,
                                  @Payload ChatMessageDTO incomingMessage,
                                  Principal principal) {
        // chatService.sendMessage broadcasts internally
        chatService.sendMessage(
                roomCode,
                incomingMessage.getUserId(),
                incomingMessage.getMessage(),
                incomingMessage.getUserName(),
                incomingMessage.getUserColor()
        );
    }

    @MessageMapping("/chat/{roomCode}/join")
    public void handleUserJoined(@DestinationVariable String roomCode,
                                 @Payload PresenceDTO presence) {
        messagingTemplate.convertAndSend("/topic/presence/" + roomCode, presence);
        chatService.broadcastUserJoined(roomCode, presence.getUserName(), presence.getUserColor());
    }

    @MessageMapping("/chat/{roomCode}/leave")
    public void handleUserLeft(@DestinationVariable String roomCode,
                               @Payload PresenceDTO presence) {
        messagingTemplate.convertAndSend("/topic/presence/" + roomCode, presence);
        chatService.broadcastUserLeft(roomCode, presence.getUserName());
    }
}
