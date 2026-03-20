package com.goatce.service;

import com.goatce.dto.ChatMessageDTO;
import com.goatce.RoomChatMessage;
import com.goatce.repository.ChatMessageRepository;
import com.goatce.repository.CollabRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final CollabRoomRepository collabRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageDTO sendMessage(String roomCode, String userId, String message, String userName, String userColor) {
        collabRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        RoomChatMessage chatMessage = RoomChatMessage.builder()
                .roomCode(roomCode)
                .userId(userId)
                .userName(userName)
                .userColor(userColor)
                .message(message)
                .build();

        RoomChatMessage saved = chatMessageRepository.save(chatMessage);

        ChatMessageDTO dto = ChatMessageDTO.builder()
                .messageId(saved.getId().toString())
                .roomCode(roomCode)
                .userId(userId)
                .userName(userName)
                .userColor(userColor)
                .message(message)
                .sentAt(saved.getSentAt())
                .type("MESSAGE")
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + roomCode, dto);
        return dto;
    }

    public List<ChatMessageDTO> getRecentMessages(String roomCode) {
        List<RoomChatMessage> messages = chatMessageRepository.findTop50ByRoomCodeOrderBySentAtDesc(roomCode);
        Collections.reverse(messages);

        return messages.stream()
                .map(m -> ChatMessageDTO.builder()
                        .messageId(m.getId().toString())
                        .roomCode(m.getRoomCode())
                        .userId(m.getUserId())
                        .userName(m.getUserName())
                        .userColor(m.getUserColor())
                        .message(m.getMessage())
                        .sentAt(m.getSentAt())
                        .type("MESSAGE")
                        .build())
                .collect(Collectors.toList());
    }

    public void broadcastUserJoined(String roomCode, String userName, String userColor) {
        ChatMessageDTO dto = ChatMessageDTO.builder()
                .roomCode(roomCode)
                .userName(userName)
                .userColor(userColor)
                .message(userName + " joined the room")
                .userId("system")
                .type("USER_JOINED")
                .sentAt(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + roomCode, dto);
    }

    public void broadcastUserLeft(String roomCode, String userName) {
        ChatMessageDTO dto = ChatMessageDTO.builder()
                .roomCode(roomCode)
                .userName(userName)
                .message(userName + " left the room")
                .userId("system")
                .type("USER_LEFT")
                .sentAt(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + roomCode, dto);
    }
}
