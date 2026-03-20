package com.goatce.repository;

import com.goatce.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OperationRepository extends JpaRepository<Operation, UUID> {
    List<Operation> findByRoomIdOrderByCreatedAtAsc(String roomId);
    List<Operation> findByRoomIdAndRevisionGreaterThan(String roomId, Integer revision);
}
