package org.example.backend.domain.aquarium.dto;

import java.time.LocalDateTime;
import org.example.backend.domain.aquarium.entity.Aquarium;

public record AquariumScheduleResponseDto(
    Long AquariumId,
    String AquariumName,
    int notifyCycleDate,
    LocalDateTime lastNotifyDate,
    LocalDateTime nextNotifyDate
) {
  public AquariumScheduleResponseDto(Aquarium aquarium) {
    this (
        aquarium.getId(),
        aquarium.getName(),
        aquarium.getCycleDate(),
        aquarium.getLastDate(),
        aquarium.getNextDate()
    );
  }
}
