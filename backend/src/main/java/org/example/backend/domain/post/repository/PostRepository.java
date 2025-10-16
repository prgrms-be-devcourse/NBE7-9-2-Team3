package org.example.backend.domain.post.repository;

import java.util.List;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.entity.Post.Displaying;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByBoardTypeAndDisplaying(BoardType boardType, Displaying displaying);
    List<Post> findByBoardType(BoardType boardType);
}
