package org.example.backend.domain.follow.repository;

import java.util.List;
import org.example.backend.domain.follow.entity.Follow;
import org.example.backend.domain.follow.entity.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    // 사용자가 다른 사용자를 팔로우하고 있는지 확인
    boolean existsByFollowerAndFollowee(Long follower, Long followee);
    
    // 팔로우 관계 삭제
    void deleteByFollowerAndFollowee(Long follower, Long followee);

    // 팔로워 수 조회
    long countByFollowee(Long followee);

    // 팔로잉 수 조회
    long countByFollower(Long follower);

    // 팔로워 목록과 멤버 정보를 함께 조회
    @Query("SELECT f, m FROM Follow f JOIN Member m ON f.follower = m.memberId WHERE f.followee = :followee")
    List<Object[]> findFollowersWithMemberInfo(@Param("followee") Long followee);

    // 팔로잉 목록과 멤버 정보를 함께 조회
    @Query("SELECT f, m FROM Follow f JOIN Member m ON f.followee = m.memberId WHERE f.follower = :follower")
    List<Object[]> findFollowingsWithMemberInfo(@Param("follower") Long follower);

    // 멤버 존재 여부 확인
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.memberId = :memberId")
    boolean existsMemberById(@Param("memberId") Long memberId);
}