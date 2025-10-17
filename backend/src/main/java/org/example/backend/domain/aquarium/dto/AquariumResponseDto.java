package org.example.backend.domain.aquarium.dto;

import java.time.LocalDateTime;
import org.example.backend.domain.aquarium.entity.Aquarium;

public record AquariumResponseDto(
    Long AquariumId,
    String AquariumName,
    LocalDateTime createDate,
    int notifyCycleDate,
    LocalDateTime lastNotifyDate,
    LocalDateTime nextNotifyDate
) {
  public AquariumResponseDto(Aquarium aquarium) {
    this (
        aquarium.getId(),
        aquarium.getName(),
        aquarium.getCreateDate(),
        aquarium.getCycleDate(),
        aquarium.getLastDate(),
        aquarium.getNextDate()
    );
  }
}
