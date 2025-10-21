package org.example.backend.domain.post.repository;

import java.util.List;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByBoardTypeAndDisplaying(BoardType boardType, Post.Displaying displaying, Pageable pageable);
    List<Post> findByBoardType(BoardType boardType);

    @Query("""
    SELECT p FROM Post p
    JOIN p.author a
    WHERE p.boardType = :boardType
      AND p.displaying = :displaying
      AND a.memberId IN :authorIds
      AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(a.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:category = 'all' OR p.category = :category)
""")
    Page<Post> searchByBoardTypeAndDisplayingAndAuthorIdInAndKeywordAndCategory(
        @Param("boardType") BoardType boardType,
        @Param("displaying") Post.Displaying displaying,
        @Param("authorIds") List<Long> authorIds,
        @Param("keyword") String keyword,
        @Param("category") String category,
        Pageable pageable
    );

    @Query("""
    SELECT p FROM Post p
    JOIN p.author a
    WHERE p.boardType = :boardType
      AND p.displaying = :displaying
      AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(a.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:category = 'all' OR p.category = :category)
""")
    Page<Post> searchByBoardTypeAndDisplayingAndKeywordAndCategory(
        @Param("boardType") BoardType boardType,
        @Param("displaying") Post.Displaying displaying,
        @Param("keyword") String keyword,
        @Param("category") String category,
        Pageable pageable
    );
}
