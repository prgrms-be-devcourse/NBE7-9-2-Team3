package org.example.backend.domain.fish.dto.response;

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
        fish.getSpecies(),
        fish.getCreateDate()
    );
  }
}
