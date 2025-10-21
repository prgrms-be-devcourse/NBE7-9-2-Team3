package org.example.backend.domain.like.repository;

import java.util.List;
import java.util.Optional;
import org.example.backend.domain.like.entity.Like;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like,Long> {

    Optional<Like> findByMemberAndPost(Member member, Post post);

    List<Like> findAllByMember(Member member);

    boolean existsByMemberAndPost(Member member, Post post);
}
