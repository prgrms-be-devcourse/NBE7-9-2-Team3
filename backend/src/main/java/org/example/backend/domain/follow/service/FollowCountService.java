package org.example.backend.domain.follow.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.follow.repository.FollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회만 하므로 readOnly = true 설정
public class FollowCountService {
    private final FollowRepository followRepository;

    // 팔로워 수 조회
    @Transactional(readOnly = true)
    public long getFollowerCount(Long memberId) {
        return followRepository.countByFolloweeMemberId(memberId);
    }

    // 팔로잉 수 조회
    @Transactional(readOnly = true)
    public long getFollowingCount(Long memberId) {
        return followRepository.countByFollowerMemberId(memberId);
    }

}
