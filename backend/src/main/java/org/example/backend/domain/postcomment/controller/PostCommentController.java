package org.example.backend.domain.postcomment.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.postcomment.dto.MyPostCommentReadResponseDto;
import org.example.backend.domain.postcomment.dto.PostCommentCreateRequestDto;
import org.example.backend.domain.postcomment.dto.PostCommentModifyRequestDto;
import org.example.backend.domain.postcomment.dto.PostCommentReadResponseDto;
import org.example.backend.domain.postcomment.entity.PostComment;
import org.example.backend.domain.postcomment.service.PostCommentService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class PostCommentController {

    private final PostCommentService postCommentService;

    // 게시글에 달린 댓글 확인
    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<List<PostCommentReadResponseDto>> getPostComments(
        @RequestParam Long postId
    ) {

        List<PostComment> comments = postCommentService.findByPostId(postId);

        List<PostCommentReadResponseDto> response = comments.stream()
            .map(c -> new PostCommentReadResponseDto(
                c.getContent(),
                c.getAuthor().getNickname()
            ))
            .toList();

        return new ApiResponse<>("200-1",
            "댓글 목록 조회",
            response
        );
    }

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public ApiResponse<List<MyPostCommentReadResponseDto>> getMyPostComments(
        @RequestParam(required = false) BoardType boardType,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        List<PostComment> postComments = postCommentService.findMyComments(userDetails.getMember(), boardType);

        List<MyPostCommentReadResponseDto> response = postComments.stream()
            .map(c -> new MyPostCommentReadResponseDto(
                c.getPost().getTitle(),
                c.getContent()
            ))
            .toList();

        return new ApiResponse<>("200-1",
            "내가 쓴 댓글 목록 조회",
            response
        );
    }

    @DeleteMapping("/{commentId}")
    @Transactional
    public ApiResponse<Void> deletePostComment(
        @PathVariable Long commentId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        PostComment postComment = postCommentService.findById(commentId)
            .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 검증
        if (!postComment.getAuthor().getMemberId().equals(userDetails.getId())) {
            throw new SecurityException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        postCommentService.deletePostComment(postComment);

        return  new ApiResponse<>(
            "200-1",
            "%d번 댓글 삭제".formatted(commentId)
        );

    }

    @PostMapping
    @Transactional
    public ApiResponse<Void> createPostComment(
        @RequestBody PostCommentCreateRequestDto reqBody,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postCommentService.createPostComment(reqBody, userDetails.getMember());

        return new ApiResponse<>(
            "201-1",
            "댓글이 생성되었습니다."
        );

    }

    @PatchMapping("/{commentId}")
    @Transactional
    public ApiResponse<Void> modifyItem(
        @PathVariable Long commentId,
        @RequestBody PostCommentModifyRequestDto reqBody,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){

        PostComment postComment = postCommentService.findById(commentId)
            .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 검증
        if (!postComment.getAuthor().getMemberId().equals(userDetails.getId())) {
            throw new SecurityException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        postCommentService.modifyPostComment(postComment, reqBody);

        return new ApiResponse<>(
            "200-1",
            "%d번 댓글이 수정되었습니다.".formatted(commentId)
        );
    }
}
