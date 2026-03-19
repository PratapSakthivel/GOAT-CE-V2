package com.goatce.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "collab_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollabRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false, unique = true)
    private String roomCode;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer revision;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.revision == null) {
            this.revision = 0;
        }
        if (this.content == null) {
            this.content = "";
        }
    }
}
