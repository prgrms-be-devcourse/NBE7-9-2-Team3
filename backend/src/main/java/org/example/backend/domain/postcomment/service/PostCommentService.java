package org.example.backend.domain.postcomment.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.service.PostService;
import org.example.backend.domain.postcomment.dto.PostCommentCreateRequestDto;
import org.example.backend.domain.postcomment.dto.PostCommentModifyRequestDto;
import org.example.backend.domain.postcomment.entity.PostComment;
import org.example.backend.domain.postcomment.repository.PostCommentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostService postService;

    public void modifyPostComment(PostComment postComment, PostCommentModifyRequestDto reqBody) {

        postComment.modifyContent(reqBody.content());
    }

    public void deletePostComment(PostComment postComment) {
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

    public List<PostComment> findByPostId(Long postId) {
        return postCommentRepository.findByPost_Id(postId);
    }
}
