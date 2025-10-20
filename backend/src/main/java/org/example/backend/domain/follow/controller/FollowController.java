package org.example.backend.domain.follow.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.follow.dto.*;
import org.example.backend.domain.follow.service.FollowService;
import org.example.backend.global.requestcontext.RequestContext;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final RequestContext requestContext;

    // 팔로우하기
    @PostMapping("/{followeeId}")
    public ResponseEntity<ApiResponse<FollowResponseDto>> follow(@PathVariable Long followeeId) {
        Long currentMemberId = requestContext.getCurrentMemberId();
        ApiResponse<FollowResponseDto> response = followService.follow(currentMemberId, followeeId);
        return ResponseEntity.ok(response);
    }

    // 언팔로우하기
    @DeleteMapping("/{followeeId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(@PathVariable Long followeeId) {
        Long currentMemberId = requestContext.getCurrentMemberId();
        ApiResponse<Void> response = followService.unfollow(currentMemberId, followeeId);
        return ResponseEntity.ok(response);
    }

    // 팔로워 목록 조회
    @GetMapping("/{memberId}/followers")
    public ResponseEntity<ApiResponse<FollowListResponseDto>> getFollowers(@PathVariable Long memberId) {
        ApiResponse<FollowListResponseDto> response = followService.getFollowers(memberId);
        return ResponseEntity.ok(response);
    }

    // 팔로잉 목록 조회
        @GetMapping("/{memberId}/followings")
    public ResponseEntity<ApiResponse<FollowListResponseDto>> getFollowings(@PathVariable Long memberId) {
        ApiResponse<FollowListResponseDto> response = followService.getFollowings(memberId);
        return ResponseEntity.ok(response);
    }

}
