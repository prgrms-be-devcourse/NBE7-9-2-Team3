package org.example.backend.domain.aquarium.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.dto.AquariumListResponseDto;
import org.example.backend.domain.aquarium.dto.AquariumCreateRequestDto;
import org.example.backend.domain.aquarium.dto.AquariumResponseDto;
import org.example.backend.domain.aquarium.dto.AquariumScheduleRequestDto;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.service.AquariumService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class AquariumController {

  private final AquariumService aquariumService;

  // 어항 생성
  @PostMapping
  public ApiResponse<AquariumListResponseDto> createAquarium(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody AquariumCreateRequestDto aquariumCreateRequestDto
  ) {
    Long memberId = userDetails.getId(); // JWT 토큰을 이용해 로그인한 member의 id를 가져옴
    String aquariumName = aquariumCreateRequestDto.aquariumName();

    Aquarium aquarium = aquariumService.create(memberId, aquariumName);
    AquariumListResponseDto aquariumListResponseDto = new AquariumListResponseDto(aquarium);

    return new ApiResponse<>(
        "201",
        "%s 어항이 생성되었습니다.".formatted(aquariumName),
        aquariumListResponseDto
    );
  }

  // 어항 다건 조회
  @GetMapping
  public ApiResponse<List<AquariumResponseDto>> getAquariums(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long memberId = userDetails.getId();

    return new ApiResponse<>(
        "200",
        "어항 목록이 조회되었습니다.",
        aquariumService.findAllByMemberId(memberId).reversed().stream()
            .map(AquariumResponseDto::new).toList()
    );
  }

  // 어항 단건 조회
  @GetMapping("/{id}")
  public ApiResponse<AquariumResponseDto> getAquariumName(@PathVariable Long id) {
    String aquariumName = aquariumService.findById(id).get().getName();
    Aquarium aquarium = aquariumService.findById(id)
        .orElseThrow(() -> new RuntimeException("어항이 존재하지 않습니다."));

    AquariumResponseDto responseDto = new AquariumResponseDto(aquarium);

    return new ApiResponse<>(
        "200",
        "%s 어항이 조회되었습니다.".formatted(aquariumName),
        responseDto
    );
  }

  // 삭제 전, 어항 속 물고기 존재 여부 확인
  @GetMapping("/{id}/delete")
  public ApiResponse<String> checkFishInAquarium(@PathVariable Long id) {
    boolean hasFish = aquariumService.hasFish(id);

    if (hasFish) {
      return new ApiResponse<>("200", "어항의 물고기 존재 여부를 확인했습니다.", "물고기 존재");
    } else {
      return new ApiResponse<>("200", "어항의 물고기 존재 여부를 확인했습니다.", "물고기 없음");
    }
  }

  // 삭제할 어항의 물고기를 '내가 키운 물고기' 어항으로 이동
  @PutMapping("/{id}/delete")
  public ApiResponse<String> moveFishToOwnedAquarium(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long id
  ) {
    Long memberId = userDetails.getId();
    aquariumService.moveFishToOwnedAquarium(memberId, id);

    return new ApiResponse<>("200", "물고기들이 '내가 키운 물고기' 어항으로 이동되었습니다.", "물고기 이동 완료");
  }

  // 어항 삭제
  @DeleteMapping("/{id}/delete")
  public ApiResponse<Void> deleteAquarium(@PathVariable Long id) {
    String aquariumName = aquariumService.findById(id).get().getName();
    aquariumService.delete(id);

    return new ApiResponse<>("204", "%s 어항이 삭제되었습니다.".formatted(aquariumName));
  }

  // 어항 알림 스케줄 설정
  @PostMapping("/{id}/schedule")
  public ApiResponse<AquariumResponseDto> scheduleSetting(
      @PathVariable Long id,
      @Valid @RequestBody AquariumScheduleRequestDto requestDto
  ) {
    Aquarium aquarium = aquariumService.scheduleSetting(id, requestDto);

    AquariumResponseDto responseDto = new AquariumResponseDto(aquarium);
    return new ApiResponse<>("200", "물갈이&어항세척 스케줄 알림이 설정되었습니다.", responseDto);
  }

}
