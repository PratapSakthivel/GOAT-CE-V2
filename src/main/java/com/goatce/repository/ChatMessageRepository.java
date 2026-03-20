package com.goatce.repository;

import com.goatce.RoomChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<RoomChatMessage, UUID> {
    List<RoomChatMessage> findByRoomCodeOrderBySentAtAsc(String roomCode);
    List<RoomChatMessage> findTop50ByRoomCodeOrderBySentAtDesc(String roomCode);
}
