package com.goatce.controller;

import com.goatce.dto.CreateRoomRequest;
import com.goatce.dto.JoinRoomResponse;
import com.goatce.dto.ParticipantInfo;
import com.goatce.dto.RoomResponse;
import com.goatce.service.CollabRoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class CollabRoomController {

    private final CollabRoomService collabRoomService;

    public CollabRoomController(CollabRoomService collabRoomService) {
        this.collabRoomService = collabRoomService;
    }

    @PostMapping("/create")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(collabRoomService.createRoom(email, request));
    }

    @PostMapping("/join/{roomCode}")
    public ResponseEntity<JoinRoomResponse> joinRoom(@PathVariable String roomCode) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(collabRoomService.joinRoom(email, roomCode));
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<JoinRoomResponse> getRoomDetails(@PathVariable String roomCode) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(collabRoomService.getRoomDetails(email, roomCode));
    }

    @PostMapping("/leave/{roomCode}")
    public ResponseEntity<String> leaveRoom(@PathVariable String roomCode) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(collabRoomService.leaveRoom(email, roomCode));
    }

    @GetMapping("/{roomCode}/participants")
    public ResponseEntity<List<ParticipantInfo>> getActiveParticipants(@PathVariable String roomCode) {
        return ResponseEntity.ok(collabRoomService.getActiveParticipants(roomCode));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
