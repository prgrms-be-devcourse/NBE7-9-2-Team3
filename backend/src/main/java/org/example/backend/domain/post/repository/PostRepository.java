package org.example.backend.domain.post.repository;

import java.util.List;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByBoardTypeAndDisplaying(BoardType boardType, Post.Displaying displaying, Pageable pageable);
    List<Post> findByBoardType(BoardType boardType);

    long countByBoardTypeAndDisplaying(BoardType boardType, Post.Displaying displaying);
}
