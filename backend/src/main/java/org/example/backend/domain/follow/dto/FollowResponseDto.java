package org.example.backend.domain.follow.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FollowResponseDto {
    private Long follower;
    private String followerNickname;
    private String followerProfileImage;
    private Long followee;
    private String followeeNickname;
    private String followeeProfileImage;

    @Builder
    public FollowResponseDto(Long follower, String followerNickname, String followerProfileImage,
                           Long followee, String followeeNickname, String followeeProfileImage) {
        this.follower = follower;
        this.followerNickname = followerNickname;
        this.followerProfileImage = followerProfileImage;
        this.followee = followee;
        this.followeeNickname = followeeNickname;
        this.followeeProfileImage = followeeProfileImage;
    }
}
