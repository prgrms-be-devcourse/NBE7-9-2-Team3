package org.example.backend.domain.fish.dto.response;

import java.time.LocalDateTime;
import org.example.backend.domain.fish.entity.Fish;

public record FishUpdateResponseDto(
    Long fishId,
    String fishSpecies,
    String fishName,
    LocalDateTime createdDate,
    LocalDateTime modifiedDate
) {
  public FishUpdateResponseDto(Fish fish) {
    this (
        fish.getId(),
        fish.getSpecies(),
        fish.getName(),
        fish.getCreateDate(),
        fish.getModifyDate()
    );
  }
}
