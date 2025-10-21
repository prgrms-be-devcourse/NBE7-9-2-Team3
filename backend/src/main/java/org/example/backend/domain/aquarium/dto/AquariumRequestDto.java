package org.example.backend.domain.aquarium.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AquariumRequestDto(
    @NotNull
    @Size(min = 1, max = 50)
    String aquariumName
) {

}
