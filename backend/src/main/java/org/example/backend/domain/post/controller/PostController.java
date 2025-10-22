package org.example.backend.domain.post.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.like.repository.LikeRepository;
import org.example.backend.domain.post.dto.MyPostReadResponseDto;
import org.example.backend.domain.post.dto.PostListResponse;
import org.example.backend.domain.post.dto.PostModifyRequestDto;
import org.example.backend.domain.post.dto.PostReadResponseDto;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.service.PostService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public ApiResponse<List<MyPostReadResponseDto>> getMyPosts(
        @RequestParam BoardType boardType,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long memberId = userDetails.getId();

        List<Post> posts = postService.findByBoardType(boardType);

        List<Post> myPosts = posts.stream()
            .filter(post -> post.getAuthor().getMemberId().equals(memberId))
            .toList();

        List<MyPostReadResponseDto> response = myPosts.stream()
            .map(post -> new MyPostReadResponseDto(
                post.getTitle(),
                post.getDisplaying()
            ))
            .toList();

        return new ApiResponse<>(
            "200-1",
            "내가 쓴 게시글 다건 조회",
            response
        );
    }

    private final LikeRepository likeRepository;

    @GetMapping
    public ApiResponse<PostListResponse> getPosts(
        @RequestParam BoardType boardType,
        @RequestParam(defaultValue = "all") String filterType, // "all" or "following"
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "all") String category,
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        PostListResponse response = postService.getPosts(
            boardType, filterType, userDetails.getMember(), keyword, category, pageable
        );

        return ApiResponse.ok("게시글 다건 조회", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<PostReadResponseDto> getPost(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        PostReadResponseDto response = postService.getPostById(id, userDetails.getMember());


        return ApiResponse.ok("%d번 게시글 단건 조회".formatted(id), response);

    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> deletePost(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postService.delete(id, userDetails.getMember());

        return ApiResponse.ok("%d번 게시글 삭제".formatted(id));

    }

    @PostMapping
    @Transactional
    public ApiResponse<Void> createPost(
        @ModelAttribute PostWriteRequestDto reqBody,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postService.write(reqBody, userDetails.getMember());

        return ApiResponse.ok("게시글 생성");

    }

    @PatchMapping("/{id}")
    @Transactional
    public ApiResponse<Void> modifyPost(
        @PathVariable Long id,
        @ModelAttribute PostModifyRequestDto reqBody,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postService.modify(id, reqBody, userDetails.getMember());

        return ApiResponse.ok("%d번 게시글 수정".formatted(id));
    }

}
