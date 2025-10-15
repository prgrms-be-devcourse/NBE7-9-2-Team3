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

  public long count() {
    return aquariumRepository.count();
  }

  public Aquarium create(Long memberId, String aquariumName) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("member가 존재하지 않습니다."));

    Aquarium aquarium = new Aquarium(member, aquariumName);
    aquariumRepository.save(aquarium);

    return aquarium;
  }

  public List<Aquarium> findAllByMemberId(Long memberId) {
    return aquariumRepository.findAllByMember_MemberId(memberId);
  }

  public Optional<Aquarium> findById(Long id) {
    return aquariumRepository.findById(id);
  }


  public boolean hasFish(Long id) {
    long fishCount = fishRepository.countByAquarium_Id(id);

    if (fishCount >= 1) {
      return true;
    } else {
      return false;
    }
  }

  public void moveFishToOwnedAquarium(Long memberId, Long aquariumId) {
    // 해당 member가 "내가 키운 물고기" 어항을 가지고 있는 지 확인
    if(checkMemberHaveOwnedAquarium(memberId)) {
      // true라면, 물고기 이동 실행
      moveFish(memberId, aquariumId);
    } else {
      // false라면, "내가 키운 물고기" 어항 생성 후 물고기 이동 실행
      createOwnedAquarium(memberId);
      moveFish(memberId, aquariumId);
    }
  }

  public void moveFish(Long memberId, Long aquariumId) {
    // 삭제할 어항의 모든 물고기 가져오기
    List<Fish> fishList = fishRepository.findAllByAquarium_Id(aquariumId);

    // '내가 키운 물고기' 어항 찾기
    Aquarium myOwnedAquarium = aquariumRepository.findByMember_MemberIdAndOwnedAquariumTrue(memberId)
        .orElseThrow(() -> new RuntimeException("'내가 키운 물고기' 어항이 존재하지 않습니다."));

    // 물고기들을 '내가 키운 물고기' 어항으로 이동
    for (Fish fish : fishList) {
      fish.changeAquarium(myOwnedAquarium);
    }
    fishRepository.saveAll(fishList);
  }

  // "내가 키운 물고기" 어항을 가지고 있는 지 확인
  public boolean checkMemberHaveOwnedAquarium(Long memberId) {
    return aquariumRepository.existsByMember_MemberIdAndOwnedAquariumTrue(memberId);
  }

  // "내가 키운 물고기" 어항 생성
  public void createOwnedAquarium(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("member가 존재하지 않습니다."));
    Aquarium aquarium = new Aquarium(member, "내가 키운 물고기", true);

    aquariumRepository.save(aquarium);
  }

  public void delete(Long id) {
    aquariumRepository.deleteById(id);
  }
}
