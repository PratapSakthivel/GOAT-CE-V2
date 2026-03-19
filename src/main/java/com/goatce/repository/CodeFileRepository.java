package com.goatce.repository;

import com.goatce.model.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CodeFileRepository extends JpaRepository<CodeFile, UUID> {
    List<CodeFile> findByFolderId(UUID folderId);
    List<CodeFile> findByOwnerEmail(String email);
    Optional<CodeFile> findByPublicToken(String token);
    List<CodeFile> findByOwnerEmailAndNameContainingIgnoreCase(String email, String name);
}
