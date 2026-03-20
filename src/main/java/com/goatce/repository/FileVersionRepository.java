package com.goatce.repository;

import com.goatce.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileVersionRepository extends JpaRepository<FileVersion, UUID> {
    List<FileVersion> findByCodeFileIdOrderByVersionNumberDesc(UUID fileId);
    Integer countByCodeFileId(UUID fileId);
}
