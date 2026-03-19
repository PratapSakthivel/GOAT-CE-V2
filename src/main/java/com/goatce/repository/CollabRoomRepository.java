package com.goatce.repository;

import com.goatce.model.CollabRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface CollabRoomRepository extends JpaRepository<CollabRoom, UUID> {
    Optional<CollabRoom> findByRoomCode(String roomCode);
    boolean existsByRoomCode(String roomCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CollabRoom c WHERE c.roomCode = :roomCode")
    Optional<CollabRoom> findByRoomCodeForUpdate(@Param("roomCode") String roomCode);
}
