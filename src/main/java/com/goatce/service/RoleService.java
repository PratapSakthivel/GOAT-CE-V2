package com.goatce.service;

import com.goatce.dto.RoleChangeRequest;
import com.goatce.CollabRoom;
import com.goatce.RoomParticipant;
import com.goatce.User;
import com.goatce.repository.CollabRoomRepository;
import com.goatce.repository.RoomParticipantRepository;
import com.goatce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoomParticipantRepository roomParticipantRepository;
    private final CollabRoomRepository collabRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public String changeRole(String requesterEmail, String roomCode, RoleChangeRequest request) {
        CollabRoom room = collabRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        RoomParticipant requesterParticipant = roomParticipantRepository.findByRoomIdAndUserId(
                room.getId(), requester.getId())
                .orElseThrow(() -> new RuntimeException("Requester not in room"));

        if (!"OWNER".equals(requesterParticipant.getRole())) {
            throw new RuntimeException("Only owner can change roles");
        }

        RoomParticipant targetParticipant = roomParticipantRepository.findByRoomIdAndUserId(
                room.getId(), UUID.fromString(request.targetUserId()))
                .orElseThrow(() -> new RuntimeException("User not in room"));

        if ("OWNER".equals(targetParticipant.getRole())) {
            throw new RuntimeException("Cannot change owner role");
        }

        targetParticipant.setRole(request.newRole());
        roomParticipantRepository.save(targetParticipant);

        return "Role updated successfully";
    }

    public String getMyRole(String email, String roomCode) {
        CollabRoom room = collabRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RoomParticipant participant = roomParticipantRepository.findByRoomIdAndUserId(
                room.getId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Not in room"));

        return participant.getRole();
    }
}
