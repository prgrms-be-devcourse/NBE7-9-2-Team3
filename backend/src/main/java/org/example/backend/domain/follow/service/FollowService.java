package org.example.backend.domain.follow.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.follow.dto.*;
import org.example.backend.domain.follow.entity.Follow;
import org.example.backend.domain.follow.repository.FollowRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.service.MemberService;
import org.example.backend.global.exception.ServiceException;
import org.example.backend.global.rsdata.RsData;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberService memberService;

    // 팔로우하기
    public RsData<FollowResponseDto> follow(Long follower, Long followee) {

        if (follower.equals(followee)) {
            throw new ServiceException("400", "자기 자신을 팔로우할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 멤버 존재 여부 확인
        if (!memberService.existsById(follower)) {
            throw new ServiceException("404", "팔로워를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        if (!memberService.existsById(followee)) {
            throw new ServiceException("404", "팔로이를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        // 이미 팔로우하고 있는지 확인
        if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new ServiceException("400", "이미 팔로우하고 있습니다.", HttpStatus.BAD_REQUEST);
        }

        Follow followEntity = Follow.builder()
            .follower(follower)
            .followee(followee)
            .build();

        Follow savedFollow = followRepository.save(followEntity);

        // 팔로우 완료 후 간단한 응답
        FollowResponseDto responseDto = FollowResponseDto.builder()
            .memberId(savedFollow.getFollowee())
            .nickname("") // 팔로우 완료 시에는 상세 정보 불필요
            .profileImage("")
            .build();

        return new RsData<>("200", "팔로우가 완료되었습니다.", responseDto);
    }

    public RsData<Void> unfollow(Long follower, Long followee) {

        if (!memberService.existsById(follower)) {
            throw new ServiceException("404", "팔로워를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        if (!memberService.existsById(followee)) {
            throw new ServiceException("404", "팔로이를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        if (!followRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new ServiceException("400", "팔로우 관계가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        followRepository.deleteByFollowerAndFollowee(follower, followee);
        return new RsData<>("200", "언팔로우가 완료되었습니다.", null);
    }


    @Transactional(readOnly = true)
    public RsData<FollowListResponseDto> getFollowers(Long memberId) {
        if (!memberService.existsById(memberId)) {
            throw new ServiceException("404", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        // 팔로워 목록과 멤버 정보를 함께 조회
        List<Object[]> results = followRepository.findFollowersWithMemberInfo(memberId);
        long totalCount = followRepository.countByFollowee(memberId);

        List<FollowResponseDto> userDtos = results.stream()
            .map(result -> {
                Member follower = (Member) result[1];
                return FollowResponseDto.builder()
                    .memberId(follower.getMemberId())
                    .nickname(follower.getNickname())
                    .profileImage(follower.getProfileImage())
                    .build();
            })
            .collect(Collectors.toList());

        FollowListResponseDto responseDto = FollowListResponseDto.builder()
            .users(userDtos)
            .totalCount(totalCount)
            .build();

        return new RsData<>("200", "팔로워 목록을 조회했습니다.", responseDto);
    }

    // 팔로잉 목록 조회
    @Transactional(readOnly = true)
    public RsData<FollowListResponseDto> getFollowings(Long memberId) {
        if (!memberService.existsById(memberId)) {
            throw new ServiceException("404", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        // 팔로잉 목록과 멤버 정보를 함께 조회
        List<Object[]> results = followRepository.findFollowingsWithMemberInfo(memberId);
        long totalCount = followRepository.countByFollower(memberId);

        List<FollowResponseDto> userDtos = results.stream()
            .map(result -> {
                Member followee = (Member) result[1];
                return FollowResponseDto.builder()
                    .memberId(followee.getMemberId())
                    .nickname(followee.getNickname())
                    .profileImage(followee.getProfileImage())
                    .build();
            })
            .collect(Collectors.toList());

        FollowListResponseDto responseDto = FollowListResponseDto.builder()
            .users(userDtos)
            .totalCount(totalCount)
            .build();

        return new RsData<>("200", "팔로잉 목록을 조회했습니다.", responseDto);
    }

    // 팔로잉 여부 확인(추후에 게시글 조회 시 사용)
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            return false;
        }
        if (followerId.equals(followeeId)) {
            return false; // 자기 자신은 팔로잉하지 않은 것으로 처리
        }
        return followRepository.existsByFollowerAndFollowee(followerId, followeeId);
    }

}
