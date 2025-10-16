package org.example.backend.domain.postcomment.repository;

import java.util.List;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.postcomment.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPost_Id(Long postId);

    List<PostComment> findByAuthor_MemberIdAndPost_BoardType(Long memberId, BoardType boardType);
}
