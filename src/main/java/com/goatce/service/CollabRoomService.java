package com.goatce.service;

import com.goatce.dto.CreateRoomRequest;
import com.goatce.dto.JoinRoomResponse;
import com.goatce.dto.ParticipantInfo;
import com.goatce.dto.RoomResponse;
import com.goatce.model.CollabRoom;
import com.goatce.model.RoomParticipant;
import com.goatce.model.User;
import com.goatce.repository.CollabRoomRepository;
import com.goatce.repository.OperationRepository;
import com.goatce.repository.RoomParticipantRepository;
import com.goatce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CollabRoomService {

    private final CollabRoomRepository collabRoomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final UserRepository userRepository;
    private final OperationRepository operationRepository;

    public CollabRoomService(CollabRoomRepository collabRoomRepository,
                             RoomParticipantRepository roomParticipantRepository,
                             UserRepository userRepository,
                             OperationRepository operationRepository) {
        this.collabRoomRepository = collabRoomRepository;
        this.roomParticipantRepository = roomParticipantRepository;
        this.userRepository = userRepository;
        this.operationRepository = operationRepository;
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (collabRoomRepository.existsByRoomCode(code));
        return code;
    }

    private String assignColor(int position) {
        return switch (position) {
            case 0 -> "#FF6B6B";
            case 1 -> "#4ECDC4";
            case 2 -> "#45B7D1";
            case 3 -> "#96CEB4";
            default -> "#FFEAA7";
        };
    }

    @Transactional
    public RoomResponse createRoom(String email, CreateRoomRequest request) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CollabRoom room = CollabRoom.builder()
                .name(request.name())
                .language(request.language())
                .roomCode(generateRoomCode())
                .owner(owner)
                .build();

        room = collabRoomRepository.save(room);

        RoomParticipant participant = RoomParticipant.builder()
                .room(room)
                .user(owner)
                .role("OWNER")
                .build();

        roomParticipantRepository.save(participant);

        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getLanguage(),
                room.getRoomCode(),
                owner.getName(),
                room.getContent(),
                room.getRevision(),
                room.getCreatedAt()
        );
    }

    @Transactional
    public JoinRoomResponse joinRoom(String email, String roomCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("DEBUG: Searching for roomCode: '" + roomCode + "'");

        CollabRoom room = collabRoomRepository.findByRoomCode(roomCode.trim())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!roomParticipantRepository.existsByRoomIdAndUserId(room.getId(), user.getId())) {
            RoomParticipant participant = RoomParticipant.builder()
                    .room(room)
                    .user(user)
                    .role("EDITOR")
                    .build();
            roomParticipantRepository.save(participant);
        }

        return getRoomDetails(email, roomCode);
    }

    public JoinRoomResponse getRoomDetails(String email, String roomCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("DEBUG (getRoomDetails): Searching for roomCode: '" + roomCode + "'");

        CollabRoom room = collabRoomRepository.findByRoomCode(roomCode.trim())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<RoomParticipant> participants = roomParticipantRepository.findByRoomId(room.getId());

        String yourRole = participants.stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .map(RoomParticipant::getRole)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User is not in the room"));

        List<ParticipantInfo> participantInfos = getActiveParticipants(roomCode);

        return new JoinRoomResponse(
                room.getId(),
                room.getRoomCode(),
                room.getName(),
                room.getLanguage(),
                room.getContent(),
                room.getRevision(),
                yourRole,
                participantInfos
        );
    }

    @Transactional
    public String leaveRoom(String email, String roomCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CollabRoom room = collabRoomRepository.findByRoomCode(roomCode.trim())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getOwner() != null && room.getOwner().getId().equals(user.getId())) {
            // If owner, delete all participants and room
            List<RoomParticipant> participants = roomParticipantRepository.findByRoomId(room.getId());
            roomParticipantRepository.deleteAll(participants);
            collabRoomRepository.delete(room);
        } else {
            // Delete only this participant
            roomParticipantRepository.deleteByRoomIdAndUserId(room.getId(), user.getId());
        }

        return "Left room successfully";
    }

    public List<ParticipantInfo> getActiveParticipants(String roomCode) {
        CollabRoom room = collabRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<RoomParticipant> participants = roomParticipantRepository.findByRoomId(room.getId());

        return java.util.stream.IntStream.range(0, participants.size())
                .mapToObj(i -> {
                    RoomParticipant p = participants.get(i);
                    return new ParticipantInfo(
                            p.getUser().getId(),
                            p.getUser().getName(),
                            p.getUser().getEmail(),
                            p.getRole(),
                            assignColor(i)
                    );
                })
                .collect(Collectors.toList());
    }
}
