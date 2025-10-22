package org.example.backend.domain.postcomment.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostService postService;

    public void modifyPostComment(Long commentId, PostCommentModifyRequestDto reqBody, Member member) {

        PostComment postComment = postCommentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 검증
        if (!postComment.getAuthor().getMemberId().equals(member.getMemberId())) {
            throw new SecurityException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        postComment.modifyContent(reqBody.content());
    }

    public void deletePostComment(Long commentId, Member member) {

        PostComment postComment = postCommentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 검증
        if (!postComment.getAuthor().getMemberId().equals(member.getMemberId())) {
            throw new SecurityException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        postCommentRepository.delete(postComment);
    }

    public void createPostComment(PostCommentCreateRequestDto reqBody, Member member) {

        Post post = postService.findById(reqBody.postId())
            .orElseThrow(() -> new RuntimeException("게시글이 없습니다"));

        PostComment postcomment = new PostComment(
            reqBody.content(),
            post,
            member
        );

        postCommentRepository.save(postcomment);
    }

    public Optional<PostComment> findById(Long id) {
        return postCommentRepository.findById(id);
    }


    public List<PostComment> findMyComments(Member member, BoardType boardType) {
        return postCommentRepository.findByAuthor_MemberIdAndPost_BoardType(member.getMemberId(), boardType);
    }

    public List<PostCommentReadResponseDto> getPostComments(Long postId, Member member) {

        List<PostComment> comments = postCommentRepository.findByPost_Id(postId);

        List<PostCommentReadResponseDto> response = comments.stream()
            .sorted(Comparator.comparing(PostComment::getCreateDate).reversed())
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
