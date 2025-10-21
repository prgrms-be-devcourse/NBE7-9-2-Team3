package org.example.backend.domain.post.controller;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.like.repository.LikeRepository;
import org.example.backend.domain.post.dto.MyPostReadResponseDto;
import org.example.backend.domain.post.dto.PostListResponse;
import org.example.backend.domain.post.dto.PostModifyRequestDto;
import org.example.backend.domain.post.dto.PostReadResponseDto;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.entity.PostImage;
import org.example.backend.domain.post.service.PostService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.data.domain.Page;
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
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        // 1차로 게시판 타입에 따라 필터링, 공개글만
        Page<Post> postPage = postService.findByBoardTypeAndDisplaying(boardType, Post.Displaying.PUBLIC, pageable);


        // 2차로 팔로잉인 경우에 한번 더 필터링
        // 팔로잉 구현 시 추가, 토큰으로 로그인 사용자를 읽어서 팔로잉 조인
        if (filterType.equals("following")) {

        }

        List<PostReadResponseDto> postDtos = postPage.getContent().stream()
            .map(post -> {
                boolean liked = likeRepository.existsByMemberAndPost(userDetails.getMember(), post);


                return new PostReadResponseDto(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getAuthor().getNickname(),
                    post.getCreateDate(),
                    post.getImages().stream().map(PostImage::getImageUrl).toList(),
                    post.getLikeCount(),
                    liked
                );
            })
            .toList();

        int totalCount = postService.countByBoardTypeAndDisplaying(boardType, Post.Displaying.PUBLIC);
        PostListResponse response = new PostListResponse(postDtos, totalCount);


        return new ApiResponse<>("200-1",
            "게시판 게시글 다건 조회",
            response
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<PostReadResponseDto> getPost(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails
        ) {
        Post post = postService.findById(id)
            .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. id=" + id));

        boolean liked = likeRepository.existsByMemberAndPost(userDetails.getMember(), post);

        PostReadResponseDto response = new PostReadResponseDto(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getAuthor().getNickname(),
            post.getCreateDate(),
            post.getImages().stream()
                .map(PostImage::getImageUrl)
                .toList(),
            post.getLikeCount(),
            liked
        );

        return new ApiResponse<>(
            "200-1",
            "%d번 id 게시글 단건 조회".formatted(id),
            response
        );

    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> deletePost(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Post post = postService.findById(id)
            .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. id=" + id));

        if (!post.getAuthor().getMemberId().equals(userDetails.getId())) {
            throw new SecurityException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        postService.delete(post);

        return new ApiResponse<>(
            "200-1",
            "%d번 게시물이 삭제되었습니다.".formatted(id)
        );

    }

    @PostMapping
    @Transactional
    public ApiResponse<Void> createPost(
        @ModelAttribute PostWriteRequestDto reqBody,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postService.write(reqBody, userDetails.getMember());

        return new ApiResponse<>(
            "200-1",
            "게시글이 생성되었습니다."
        );
    }

    @PatchMapping("/{id}")
    @Transactional
    public ApiResponse<Void> modifyPost(
        @PathVariable Long id,
        @ModelAttribute PostModifyRequestDto reqBody,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Post post = postService.findById(id)
            .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. id=" + id));

        // 작성자 검증
        if (!post.getAuthor().getMemberId().equals(userDetails.getId())) {
            throw new SecurityException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        postService.modify(post, reqBody);
        return new ApiResponse<>(
            "200-1",
            "게시글이 수정되었습니다."
        );
    }

}
