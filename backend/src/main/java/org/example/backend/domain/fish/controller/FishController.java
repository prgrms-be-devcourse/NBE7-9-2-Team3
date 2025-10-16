package org.example.backend.domain.fish.controller;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.fish.dto.request.FishRequestDto;
import org.example.backend.domain.fish.dto.response.FishCreateResponseDto;
import org.example.backend.domain.fish.dto.response.FishListResponseDto;
import org.example.backend.domain.fish.dto.response.FishUpdateResponseDto;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.service.FishService;
import org.example.backend.global.rsdata.RsData;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aquarium")
public class FishController {

  private final FishService fishService;

  // 물고기 생성
  @PostMapping("/{aquariumId}/fish")
  public RsData<FishCreateResponseDto> createFish(
      @PathVariable Long aquariumId,
      @RequestBody FishRequestDto fishRequestDto
  ) {
    Fish fish = fishService.createFish(aquariumId, fishRequestDto);
    FishCreateResponseDto fishCreateResponseDto = new FishCreateResponseDto(fish);

    return new RsData<>("201", "물고기가 생성되었습니다.", fishCreateResponseDto);
  }

  // 물고기 다건 조회
  @GetMapping("/{aquariumId}/fish")
  public RsData<List<FishListResponseDto>> getFishes(@PathVariable Long aquariumId) {

    return new RsData<>(
        "200",
        "물고기들이 조회되었습니다.",
        fishService.findAllByAquariumId(aquariumId).reversed().stream()
            .map(FishListResponseDto::new).toList()
    );
  }

  // 물고기 수정
  @PutMapping("/{aquariumId}/fish/{fishId}")
  public RsData<FishUpdateResponseDto> updateFish(
      @PathVariable Long aquariumId, @PathVariable Long fishId,
      @RequestBody FishRequestDto fishRequestDto
  ) {
    Fish fish = fishService.updateFish(aquariumId, fishId, fishRequestDto);
    FishUpdateResponseDto fishUpdateResponseDto = new FishUpdateResponseDto(fish);

    return new RsData<>(
        "200",
        "물고기 종과 이름이 수정되었습니다.",
        fishUpdateResponseDto
    );
  }

  // 물고기 삭제
  @DeleteMapping("/{aquariumId}/fish/{fishId}")
  public RsData<Void> deleteFish(@PathVariable Long aquariumId, @PathVariable Long fishId) {
    fishService.deleteFish(aquariumId, fishId);

    return new RsData<>("204", "물고기가 삭제되었습니다.");
  }
}
