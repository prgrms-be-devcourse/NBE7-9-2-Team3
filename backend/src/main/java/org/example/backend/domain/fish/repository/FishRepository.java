package org.example.backend.domain.fish.repository;

import java.util.List;
import org.example.backend.domain.fish.entity.Fish;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FishRepository extends JpaRepository<Fish, Long> {

  long countByAquariumId(Long aquariumId);

  List<Fish> findAllByAquariumId(Long aquariumId);

}
