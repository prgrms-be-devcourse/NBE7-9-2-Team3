package org.example.backend.domain.aquarium.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "aquarium_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AquariumLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "aquarium_id", nullable = false)
    private Long aquariumId;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "ph")
    private Double ph;

    @Column(name = "log_date", nullable = false)
    private LocalDateTime logDate;
}
