package com.goatce.controller;

import com.goatce.dto.ChatMessageDTO;
import com.goatce.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{roomCode}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getRecentMessages(@PathVariable String roomCode) {
        return ResponseEntity.ok(chatService.getRecentMessages(roomCode));
    }
}
