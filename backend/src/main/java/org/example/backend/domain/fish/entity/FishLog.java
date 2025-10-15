package org.example.backend.domain.fish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fish_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FishLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "aquarium_id", nullable = false)
    private Long aquariumId;

    @Column(name = "fish_id", nullable = false)
    private Long fishId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "log_date", nullable = false)
    private LocalDateTime logDate;
    
    @PrePersist
    protected void onCreate() {
        logDate = LocalDateTime.now();
    }
}
