package com.goatce.repository;

import com.goatce.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    List<Folder> findByOwnerEmail(String email);
    boolean existsByNameAndOwnerEmail(String name, String email);
}
