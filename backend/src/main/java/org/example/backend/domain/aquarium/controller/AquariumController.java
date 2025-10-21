package org.example.backend.domain.aquarium.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.dto.AquariumListResponseDto;
import org.example.backend.domain.aquarium.dto.AquariumCreateRequestDto;
import org.example.backend.domain.aquarium.dto.AquariumResponseDto;
import org.example.backend.domain.aquarium.dto.AquariumScheduleRequestDto;
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
@Tag(name = "Aquarium", description = "어항 관리 API")
public class AquariumController {

  private final AquariumService aquariumService;

  // 어항 생성
  @Operation(summary = "어항 생성", description = "새로운 어항을 생성합니다.")
  @PostMapping
  public ApiResponse<AquariumListResponseDto> createAquarium(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody AquariumCreateRequestDto requestDto
  ) {
    AquariumListResponseDto responseDto = aquariumService.create(userDetails, requestDto);

    return ApiResponse.ok("어항이 생성되었습니다.", responseDto);
  }

  // 어항 다건 조회
  @Operation(summary = "어항 목록 조회", description = "로그인한 회원의 모든 어항을 조회합니다.")
  @GetMapping
  public ApiResponse<List<AquariumResponseDto>> getAquariums(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<AquariumResponseDto> responseDto = aquariumService.findAllByMemberId(userDetails);

    return ApiResponse.ok("어항 목록이 조회되었습니다.", responseDto);
  }

  // 어항 단건 조회
  @Operation(summary = "어항 조회", description = "특정 어항의 상세 정보를 조회합니다.")
  @GetMapping("/{id}")
  public ApiResponse<AquariumResponseDto> getAquariumName(@PathVariable Long id) {
    AquariumResponseDto responseDto = aquariumService.findById(id);

    return ApiResponse.ok("어항이 조회되었습니다.", responseDto);
  }

  // 삭제 전, 어항 속 물고기 존재 여부 확인
  @Operation(summary = "어항 속 물고기 존재 여부 확인", description = "특정 어항의 물고기 존재 여부를 확인합니다.")
  @GetMapping("/{id}/delete")
  public ApiResponse<Boolean> checkFishInAquarium(@PathVariable Long id) {
    boolean hasFish = aquariumService.hasFish(id);

    return ApiResponse.ok("어항의 물고기 존재 여부를 확인했습니다.", hasFish);
  }

  // 삭제할 어항의 물고기를 '내가 키운 물고기' 어항으로 이동
  @Operation(summary = "물고기 이동", description = "삭제할 어항 속 물고기들을 '내가 키운 물고기' 어항으로 이동시킵니다.")
  @PutMapping("/{id}/delete")
  public ApiResponse<String> moveFishToOwnedAquarium(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long id
  ) {
    aquariumService.moveFishToOwnedAquarium(userDetails, id);

    return ApiResponse.ok("물고기들이 '내가 키운 물고기' 어항으로 이동되었습니다.", "물고기 이동 완료");
  }

  // 어항 삭제
  @DeleteMapping("/{id}/delete")
  @Operation(summary = "어항 삭제", description = "특정 어항을 삭제합니다.")
  public ApiResponse<Void> deleteAquarium(@PathVariable Long id) {
    aquariumService.delete(id);

    return ApiResponse.ok("어항이 삭제되었습니다.");
  }

  // 어항 알림 스케줄 설정
  @PostMapping("/{id}/schedule")
  @Operation(summary = "어항 알림 스케줄 설정", description = "특정 어항의 알림 스케줄을 설정합니다.")
  public ApiResponse<AquariumResponseDto> scheduleSetting(
      @PathVariable Long id,
      @Valid @RequestBody AquariumScheduleRequestDto requestDto
  ) {
    AquariumResponseDto responseDto = aquariumService.scheduleSetting(id, requestDto);

    return ApiResponse.ok("물갈이&어항세척 스케줄 알림이 설정되었습니다.", responseDto);
  }

}
