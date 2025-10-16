package org.example.backend.domain.follow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 팔로우 ID 클래스
public class FollowId implements Serializable {
    private Long follower;
    private Long followee;
}
