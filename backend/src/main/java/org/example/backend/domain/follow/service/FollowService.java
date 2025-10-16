package org.example.backend.domain.follow.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.follow.dto.*;
import org.example.backend.domain.follow.entity.Follow;
import org.example.backend.domain.follow.repository.FollowRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.global.exception.ServiceException;
import org.example.backend.global.rsdata.RsData;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    // 팔로우하기
    public RsData<FollowResponseDto> follow(Long follower, Long followee) {

        if (follower.equals(followee)) {
            throw new ServiceException("400", "자기 자신을 팔로우할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        Member followerMember = memberRepository.findById(follower)
            .orElseThrow(
                () -> new ServiceException("404", "팔로워를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Member followeeMember = memberRepository.findById(followee)
            .orElseThrow(
                () -> new ServiceException("404", "팔로이를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new ServiceException("400", "이미 팔로우하고 있습니다.", HttpStatus.BAD_REQUEST);
        }

        Follow followEntity = Follow.builder()
            .follower(follower)
            .followee(followee)
            .build();

        followEntity.validateNotSelfFollow();
        followRepository.save(followEntity);

        FollowResponseDto responseDto = FollowResponseDto.builder()
            .follower(followerMember.getMemberId())
            .followerNickname(followerMember.getNickname())
            .followerProfileImage(followerMember.getProfileImage())
            .followee(followeeMember.getMemberId())
            .followeeNickname(followeeMember.getNickname())
            .followeeProfileImage(followeeMember.getProfileImage())
            .build();

        return new RsData<>("200", "팔로우가 완료되었습니다.", responseDto);
    }

    public RsData<Void> unfollow(Long follower, Long followee) {

        if (!memberRepository.existsById(follower)) {
            throw new ServiceException("404", "팔로워를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        if (!memberRepository.existsById(followee)) {
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
        Member member = memberRepository.findById(memberId)
            .orElseThrow(
                () -> new ServiceException("404", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        List<Follow> follows = followRepository.findByFollowee(memberId);
        long totalCount = followRepository.countByFollowee(memberId);

        // 팔로워들의 ID 수집
        List<Long> followerIds = follows.stream()
            .map(Follow::getFollower)
            .collect(Collectors.toList());

        // 팔로워들의 정보 조회
        Map<Long, Member> followerMap = memberRepository.findAllById(followerIds)
            .stream()
            .collect(Collectors.toMap(Member::getMemberId, follower -> follower));

        List<FollowResponseDto> followDtos = follows.stream()
            .map(follow -> {
                Member follower = followerMap.get(follow.getFollower());
                return FollowResponseDto.builder()
                    .follower(follow.getFollower())
                    .followerNickname(follower.getNickname())
                    .followerProfileImage(follower.getProfileImage())
                    .followee(follow.getFollowee())
                    .followeeNickname(member.getNickname())
                    .followeeProfileImage(member.getProfileImage())
                    .build();
            })
            .collect(Collectors.toList());

        FollowListResponseDto responseDto = FollowListResponseDto.builder()
            .follows(followDtos)
            .totalCount(totalCount)
            .build();

        return new RsData<>("200", "팔로워 목록을 조회했습니다.", responseDto);
    }

    // 팔로잉 목록 조회
    @Transactional(readOnly = true)
    public RsData<FollowListResponseDto> getFollowings(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(
                () -> new ServiceException("404", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        List<Follow> follows = followRepository.findByFollower(memberId);
        long totalCount = followRepository.countByFollower(memberId);

        // 팔로이들의 ID 수집
        List<Long> followeeIds = follows.stream()
            .map(Follow::getFollowee)
            .collect(Collectors.toList());

        // 팔로이들의 정보 조회
        Map<Long, Member> followeeMap = memberRepository.findAllById(followeeIds)
            .stream()
            .collect(Collectors.toMap(Member::getMemberId, followee -> followee));

        List<FollowResponseDto> followDtos = follows.stream()
            .map(follow -> {
                Member followee = followeeMap.get(follow.getFollowee());
                return FollowResponseDto.builder()
                    .follower(follow.getFollower())
                    .followerNickname(member.getNickname())
                    .followerProfileImage(member.getProfileImage())
                    .followee(follow.getFollowee())
                    .followeeNickname(followee.getNickname())
                    .followeeProfileImage(followee.getProfileImage())
                    .build();
            })
            .collect(Collectors.toList());

        FollowListResponseDto responseDto = FollowListResponseDto.builder()
            .follows(followDtos)
            .totalCount(totalCount)
            .build();

        return new RsData<>("200", "팔로잉 목록을 조회했습니다.", responseDto);
    }

    // 팔로워 수 조회
    @Transactional(readOnly = true)
    public long getFollowerCount(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ServiceException("404", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        return followRepository.countByFollowee(memberId);
    }

    // 팔로잉 수 조회
    @Transactional(readOnly = true)
    public long getFollowingCount(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ServiceException("404", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        return followRepository.countByFollower(memberId);
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
