package org.example.backend.domain.fish.controller;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.fish.dto.FishRequestDto;
import org.example.backend.domain.fish.dto.FishResponseDto;
import org.example.backend.domain.fish.dto.FishUpdateResponseDto;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.service.FishService;
import org.example.backend.global.response.ApiResponse;
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
@RequestMapping("/api/aquarium/{aquariumId}/fish")
public class FishController {

  private final FishService fishService;

  // 물고기 생성
  @PostMapping()
  public ApiResponse<FishResponseDto> createFish(
      @PathVariable Long aquariumId,
      @RequestBody FishRequestDto fishRequestDto
  ) {
    FishResponseDto responseDto = fishService.createFish(aquariumId, fishRequestDto);

    return ApiResponse.ok("물고기가 생성되었습니다.", responseDto);
  }

  // 물고기 다건 조회
  @GetMapping()
  public ApiResponse<List<FishResponseDto>> getFishes(@PathVariable Long aquariumId) {

    List<FishResponseDto> responseDto = fishService.findAllByAquariumId(aquariumId);

    return ApiResponse.ok("물고기들이 조회되었습니다.", responseDto);
  }

  // 물고기 수정
  @PutMapping("/{fishId}")
  public ApiResponse<FishUpdateResponseDto> updateFish(
      @PathVariable Long aquariumId, @PathVariable Long fishId,
      @RequestBody FishRequestDto fishRequestDto
  ) {
    FishUpdateResponseDto responseDto = fishService.updateFish(aquariumId, fishId, fishRequestDto);

    return ApiResponse.ok("물고기 종과 이름이 수정되었습니다.", responseDto);
  }

  // 물고기 삭제
  @DeleteMapping("/{fishId}")
  public ApiResponse<Void> deleteFish(@PathVariable Long aquariumId, @PathVariable Long fishId) {
    fishService.deleteFish(aquariumId, fishId);

    return ApiResponse.ok("물고기가 삭제되었습니다.");
  }
}
