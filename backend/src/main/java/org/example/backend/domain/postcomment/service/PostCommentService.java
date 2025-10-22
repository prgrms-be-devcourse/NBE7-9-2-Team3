package org.example.backend.domain.postcomment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.service.PostService;
import org.example.backend.domain.postcomment.dto.PostCommentCreateRequestDto;
import org.example.backend.domain.postcomment.dto.PostCommentModifyRequestDto;
import org.example.backend.domain.postcomment.dto.PostCommentReadResponseDto;
import org.example.backend.domain.postcomment.entity.PostComment;
import org.example.backend.domain.postcomment.repository.PostCommentRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostService postService;

    @Transactional
    public void modifyPostComment(Long commentId, PostCommentModifyRequestDto reqBody, Member member) {

        PostComment postComment = postCommentRepository.findByIdWithAuthor(commentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_DATA));

        // 작성자 검증
        if (!postComment.getAuthor().getMemberId().equals(member.getMemberId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        postComment.modifyContent(reqBody.content());
    }

    @Transactional
    public void deletePostComment(Long commentId, Member member) {

        PostComment postComment = postCommentRepository.findByIdWithAuthor(commentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_DATA));

        // 작성자 검증
        if (!postComment.getAuthor().getMemberId().equals(member.getMemberId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        postCommentRepository.delete(postComment);
    }

    @Transactional
    public void createPostComment(PostCommentCreateRequestDto reqBody, Member member) {

        Post post = postService.findById(reqBody.postId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_DATA));

        PostComment postcomment = new PostComment(
            reqBody.content(),
            post,
            member
        );

        postCommentRepository.save(postcomment);
    }

    public List<PostComment> findMyComments(Member member, BoardType boardType) {
        if (boardType == null) {
            // boardType이 null이면 모든 댓글 조회
            return postCommentRepository.findByAuthor_MemberId(member.getMemberId());
        } else {
            // boardType이 지정되면 해당 게시판의 댓글만 조회
            return postCommentRepository.findByAuthor_MemberIdAndPost_BoardType(member.getMemberId(), boardType);
        }
    }

    @Transactional(readOnly = true)
    public List<PostCommentReadResponseDto> getPostComments(Long postId, Member member) {

        List<PostComment> comments = postCommentRepository.findByPostIdWithAuthor(postId);

        List<PostCommentReadResponseDto> response = comments.stream()
            .map(c -> new PostCommentReadResponseDto(
                c.getId(),
                c.getContent(),
                c.getAuthor().getNickname(),
                c.getAuthor().getMemberId().equals(member.getMemberId())
            ))
            .toList();

        return response;
    }
}
