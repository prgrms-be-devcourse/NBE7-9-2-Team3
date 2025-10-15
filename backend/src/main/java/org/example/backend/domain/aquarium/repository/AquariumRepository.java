package org.example.backend.domain.aquarium.repository;

import java.util.List;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AquariumRepository extends JpaRepository<Aquarium, Long> {

  List<Aquarium> findAllByMemberId(Long memberId);

}
