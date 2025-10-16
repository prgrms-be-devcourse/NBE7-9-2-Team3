package org.example.backend.domain.follow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "follow", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"follower", "followee"}))
@IdClass(FollowId.class)
public class Follow {

    @Id
    @Column(name = "follower")
    private Long follower;
    
    @Id
    @Column(name = "followee")
    private Long followee;


    @Builder
    public Follow(Long follower, Long followee) {
        this.follower = follower;
        this.followee = followee;
    }

    // 자기 자신을 팔로우하는 것을 방지하는 로직
    public void validateNotSelfFollow() {
        if (this.follower.equals(this.followee)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }
    }
}
