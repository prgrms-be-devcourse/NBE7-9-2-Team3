package org.example.backend.domain.fish.dto;

import java.time.LocalDateTime;
import org.example.backend.domain.fish.entity.Fish;

public record FishCreateResponseDto(
    Long fishId,
    String fishSpecies,
    String fishName,
    LocalDateTime createDate
) {

  public FishCreateResponseDto(Fish fish) {
    this(
        fish.getId(),
        fish.getSpecies(),
        fish.getName(),
        fish.getCreateDate()
    );
  }
}
