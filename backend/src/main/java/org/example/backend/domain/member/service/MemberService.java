package org.example.backend.domain.member.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.follow.service.FollowCountService;
import org.example.backend.domain.member.dto.MemberEditRequestDto;
import org.example.backend.domain.member.dto.MemberEditResponseDto;
import org.example.backend.domain.member.dto.MemberJoinRequestDto;
import org.example.backend.domain.member.dto.MemberJoinResponseDto;
import org.example.backend.domain.member.dto.MemberLoginRequestDto;
import org.example.backend.domain.member.dto.MemberLoginResponseDto;
import org.example.backend.domain.member.dto.MemberResponseDto;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.image.ImageService;
import org.example.backend.global.response.ApiResponse;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final FollowCountService followCountService;
    private final ImageService imageService;

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findByNickname(String nickname) {
        return memberRepository.findByNickname(nickname);
    }

    public Optional<Member> findByMemberId(Long memberId) {
        return memberRepository.findByMemberId(memberId);
    }

    // 멤버 존재하지 않음 확인
    public boolean notExistsById(Long memberId) {
        return !memberRepository.existsById(memberId);
    }

    // 현재 인증된 사용자 ID 가져오기
    private Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getMember().getMemberId();
    }

    @Transactional
    public Member create(String email, String password, String nickname, String profileImage) {
        // 이메일 중복 체크
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATE);
        }

        // 닉네임 중복 체크
        if (memberRepository.findByNickname(nickname).isPresent()) {
            throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATE);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 회원 생성
        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .profileImage(profileImage)
                .build();

        return memberRepository.save(member);
    }

    @Transactional
    public ApiResponse<MemberJoinResponseDto> join(MemberJoinRequestDto request, MultipartFile profileImage) {
        String profileImageUrl = null;
        
        // 프로필 이미지가 있으면 S3에 업로드
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = imageService.uploadFile(profileImage, "profile");
        }
        
        Member savedMember = create(
            request.email(),
            request.password(),
            request.nickname(),
            profileImageUrl
        );

        MemberJoinResponseDto response = MemberJoinResponseDto.from(savedMember);
        return ApiResponse.ok("회원가입이 완료되었습니다.", response);
    }

    public ApiResponse<MemberLoginResponseDto> login(MemberLoginRequestDto request) {
        Member member = memberRepository.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH);
        }

        MemberLoginResponseDto response = new MemberLoginResponseDto(
            member.getMemberId(),
            member.getEmail(),
            member.getNickname(),
            member.getProfileImage()
        );
        return ApiResponse.ok("로그인에 성공했습니다.", response);
    }

    // JWT 토큰을 별도로 생성하는 메서드
    public String generateAccessToken(Member member) {
        return authTokenService.genAccessToken(member);
    }

    @Transactional
    public ApiResponse<MemberEditResponseDto> edit(MemberEditRequestDto request, MultipartFile profileImage){
        // 현재 로그인한 사용자 조회
        Member member = memberRepository.findByMemberId(getCurrentMemberId())
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        //  현재 비밀번호 재확인
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH);
        }

        // 이메일이나 닉네임이 변경되는지 확인
        boolean emailChanged = !member.getEmail().equals(request.email());
        boolean nicknameChanged = !member.getNickname().equals(request.nickname());

        // 이메일 중복 체크 (현재 사용자와 다른 이메일인 경우)
        if (emailChanged) {
            if (memberRepository.findByEmail(request.email()).isPresent()) {
                throw new BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATE);
            }
        }

        // 닉네임 중복 체크 (현재 사용자와 다른 닉네임인 경우)
        if (nicknameChanged) {
            if (memberRepository.findByNickname(request.nickname()).isPresent()) {
                throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATE);
            }
        }

        // 새로운 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.newPassword());

        // 프로필 이미지 처리
        String profileImageUrl = request.profileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            // 새로운 이미지가 있으면 S3에 업로드
            profileImageUrl = imageService.uploadFile(profileImage, "profile");
        }

        // 회원 정보 업데이트
        member.updateMemberInfo(
            request.email(),
            encodedPassword,
            request.nickname(),
            profileImageUrl
        );

        // 데이터베이스에 저장
        Member updatedMember = memberRepository.save(member);

        // 토큰 정보가 변경된 경우 새로운 토큰 발급 (컨트롤러에서 처리)
        String newAccessToken = null;
        if (emailChanged || nicknameChanged) {
            newAccessToken = authTokenService.genAccessToken(updatedMember);
        }

        // 응답 DTO 생성
        MemberEditResponseDto response = MemberEditResponseDto.from(updatedMember, newAccessToken);
        return ApiResponse.ok("회원정보 수정에 성공했습니다.", response);
    }
    @Transactional
    public ApiResponse<MemberResponseDto> myPage(){
        // 현재 로그인한 사용자 조회
        Member member = memberRepository.findByMemberId(getCurrentMemberId())
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        MemberResponseDto response = new MemberResponseDto(member,followCountService.getFollowerCount(member.getMemberId()),followCountService.getFollowingCount(member.getMemberId()));
        return ApiResponse.ok("회원 정보 조회에 성공했습니다.", response);
    }


}
