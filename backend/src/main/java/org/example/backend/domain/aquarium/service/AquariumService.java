package org.example.backend.domain.aquarium.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.repository.FishRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AquariumService {

  private final AquariumRepository aquariumRepository;
  private final MemberRepository memberRepository;
  private final FishRepository fishRepository;

  public Aquarium create(Long memberId, String aquariumName) {
    Member member = memberRepository.findById(memberId).orElse(null);

    Aquarium aquarium = new Aquarium(member, aquariumName);
    aquariumRepository.save(aquarium);

    return aquarium;
  }

  public List<Aquarium> findAllByMemberId(Long memberId) {
    return aquariumRepository.findAllByMemberId(memberId);
  }

  public Optional<Aquarium> findById(Long id) {
    return aquariumRepository.findById(id);
  }


  public boolean hasFish(Long id) {
    long fishCount = fishRepository.countByAquariumId(id);

    if(fishCount >= 1) { return true; }
    else { return false; }
  }

  public void moveFishToOwned(Long id) {
    // 삭제할 어항의 모든 물고기 가져오기
    List<Fish> fishList = fishRepository.findAllByAquariumId(id);

    // '내가 키운 물고기' 어항은 항상 DB 첫 번째 어항(ID = 1)
    Aquarium myOwnedAquarium = aquariumRepository.findById(1L)
        .orElseThrow(() -> new RuntimeException("'내가 키운 물고기' 어항이 존재하지 않습니다."));

    // 물고기들을 '내가 키운 물고기' 어항으로 이동
    for (Fish fish : fishList) {
      fish.changeAquarium(myOwnedAquarium);
    }
    fishRepository.saveAll(fishList);
  }

  public void delete(Long id) {
    aquariumRepository.deleteById(id);
  }
}
