package com.goatce.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_versions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private CodeFile codeFile;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private Integer versionNumber;

    @Column(updatable = false)
    private LocalDateTime savedAt;

    @PrePersist
    public void prePersist() {
        this.savedAt = LocalDateTime.now();
    }
}
