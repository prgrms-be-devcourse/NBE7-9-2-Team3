package org.example.backend.domain.fish.dto;

import org.example.backend.domain.fish.entity.Fish;

public record FishListResponseDto(
    String fishSpecies,
    String fishName
) {

  public FishListResponseDto(Fish fish) {
    this(
        fish.getSpecies(),
        fish.getName()
    );
  }
}
