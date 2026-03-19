package com.goatce.repository;

import com.goatce.model.CollabRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CollabRoomRepository extends JpaRepository<CollabRoom, UUID> {
    Optional<CollabRoom> findByRoomCode(String roomCode);
    boolean existsByRoomCode(String roomCode);
}
