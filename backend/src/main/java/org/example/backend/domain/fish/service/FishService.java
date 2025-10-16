package org.example.backend.domain.fish.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.fish.dto.FishRequestDto;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.repository.FishRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FishService {

  private final FishRepository fishRepository;
  private final AquariumRepository aquariumRepository;

  public long count() {
    return fishRepository.count();
  }

  public Fish createFish(Long aquariumId, FishRequestDto fishRequestDto) {
    String species = fishRequestDto.species();
    String name = fishRequestDto.name();

    Aquarium aquarium = aquariumRepository.findById(aquariumId)
        .orElseThrow(() -> new RuntimeException("어항이 존재하지 않습니다."));
    Fish fish = new Fish(aquarium, species, name);

    fishRepository.save(fish);

    return fish;
  }

  public List<Fish> findAllByAquariumId(Long aquariumId) {
    return fishRepository.findAllByAquarium_Id(aquariumId);
  }

  public Fish updateFish(Long aquariumId, Long fishId, FishRequestDto fishRequestDto) {
    Fish fish = fishRepository.findByAquarium_IdAndId(aquariumId, fishId)
        .orElseThrow(() -> new RuntimeException("물고기가 존재하지 않습니다."));

    String species = fishRequestDto.species();
    String name = fishRequestDto.name();

    fish.changeDetails(species, name);
    fishRepository.save(fish);

    return fish;
  }

  public void deleteFish(Long aquariumId, Long fishId) {
    Fish fish = fishRepository.findByAquarium_IdAndId(aquariumId, fishId)
        .orElseThrow(() -> new RuntimeException("물고기가 존재하지 않습니다."));

    fishRepository.delete(fish);
  }

}
