package org.example.backend.domain.aquarium.repository;

import java.util.List;
import java.util.Optional;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AquariumRepository extends JpaRepository<Aquarium, Long> {

  List<Aquarium> findAllByMember_MemberId(Long memberId);

  boolean existsByMember_MemberIdAndOwnedAquariumTrue(Long memberId);

  Optional<Aquarium> findByMember_MemberIdAndOwnedAquariumTrue(Long memberId);

}
