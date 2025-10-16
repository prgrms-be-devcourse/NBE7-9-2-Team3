package org.example.backend.domain.follow.repository;

import org.example.backend.domain.follow.entity.Follow;
import org.example.backend.domain.follow.entity.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    // 특정 사용자가 다른 사용자를 팔로우하고 있는지 확인
    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.follower = :follower AND f.followee = :followee")
    boolean existsByFollowerAndFollowee(@Param("follower") Long follower, @Param("followee") Long followee);
    
    // 특정 사용자를 팔로우하는 사람들 조회 (팔로워 목록)
    @Query("SELECT f FROM Follow f WHERE f.followee = :followee")
    List<Follow> findByFollowee(@Param("followee") Long followee);

    // 특정 사용자가 팔로우하는 사람들 조회 (팔로잉 목록)
    @Query("SELECT f FROM Follow f WHERE f.follower = :follower")
    List<Follow> findByFollower(@Param("follower") Long follower);

    // 팔로우 관계 삭제 (ID 기반)
    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower = :follower AND f.followee = :followee")
    void deleteByFollowerAndFollowee(@Param("follower") Long follower, @Param("followee") Long followee);

    // 특정 사용자의 팔로워 수 조회
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followee = :followee")
    long countByFollowee(@Param("followee") Long followee);

    // 특정 사용자의 팔로잉 수 조회
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower = :follower")
    long countByFollower(@Param("follower") Long follower);

    // 팔로우 관계 조회 (ID 기반)
    @Query("SELECT f FROM Follow f WHERE f.follower = :follower AND f.followee = :followee")
    Optional<Follow> findByFollowerAndFollowee(@Param("follower") Long follower, @Param("followee") Long followee);
}
