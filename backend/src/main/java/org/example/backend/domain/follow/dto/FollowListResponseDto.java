package org.example.backend.domain.follow.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FollowListResponseDto {
    private List<FollowResponseDto> follows;
    private long totalCount;

    @Builder
    public FollowListResponseDto(List<FollowResponseDto> follows, long totalCount) {
        this.follows = follows;
        this.totalCount = totalCount;
    }
}
