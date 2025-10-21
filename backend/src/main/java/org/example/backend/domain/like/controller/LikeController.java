package org.example.backend.domain.like.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.like.dto.PostLikeResponseDto;
import org.example.backend.domain.like.service.LikeService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}/likes")
    public Map<String, Object> toggleLike(
        @PathVariable Long postId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return likeService.toggleLike(postId, userDetails.getId());
    }

    @GetMapping("/likes/my")
    public ApiResponse<List<PostLikeResponseDto>> getLikedPosts(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PostLikeResponseDto> likedPosts = likeService.getLikedPosts(userDetails.getId());
        return ApiResponse.ok("좋아요한 글 조회 성공", likedPosts);
    }
}
