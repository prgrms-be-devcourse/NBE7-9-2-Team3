package org.example.backend.domain.aquarium.dto;

import java.time.LocalDateTime;
import org.example.backend.domain.aquarium.entity.Aquarium;

public record AquariumDto(
    Long AquariumId,
    String AquariumName,
    LocalDateTime createDate
) {

  public AquariumDto(Aquarium aquarium) {
    this(
        aquarium.getAquariumId(),
        aquarium.getName(),
        aquarium.getCreateDate()
    );
  }
}
