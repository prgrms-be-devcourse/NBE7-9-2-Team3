package org.example.backend.domain.fish.dto;

import org.example.backend.domain.fish.entity.Fish;

public record FishListResponseDto(
    Long fishId,
    String fishSpecies,
    String fishName
) {

  public FishListResponseDto(Fish fish) {
    this(
        fish.getId(),
        fish.getSpecies(),
        fish.getName()
    );
  }
}
