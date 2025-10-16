package org.example.backend.domain.member.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.dto.MemberEditRequestDto;
import org.example.backend.domain.member.dto.MemberJoinRequestDto;
import org.example.backend.domain.member.dto.MemberJoinResponseDto;
import org.example.backend.domain.member.dto.MemberLoginRequestDto;
import org.example.backend.domain.member.dto.MemberLoginResponseDto;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.global.exception.ServiceException;
import org.example.backend.global.requestcontext.RequestContext;
import org.example.backend.global.rsdata.RsData;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final RequestContext requestContext;

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findByNickname(String nickname) {
        return memberRepository.findByNickname(nickname);
    }

    @Transactional
    public Member create(String email, String password, String nickname, String profileImage) {
        // 이메일 중복 체크
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new ServiceException("409", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT);
        }

        // 닉네임 중복 체크
        if (memberRepository.findByNickname(nickname).isPresent()) {
            throw new ServiceException("409", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT);
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
    public RsData<MemberJoinResponseDto> join(MemberJoinRequestDto request) {
        Member savedMember = create(
            request.email(),
            request.password(),
            request.nickname(),
            request.profileImage()
        );

        MemberJoinResponseDto response = MemberJoinResponseDto.from(savedMember);
        return new RsData<>("201", "회원가입이 완료되었습니다.", response);
    }

    public RsData<MemberLoginResponseDto> login(MemberLoginRequestDto request) {
        Member member = memberRepository.findByEmail(request.email())
            .orElseThrow(() -> new ServiceException("404", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new ServiceException("401", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        MemberLoginResponseDto response = new MemberLoginResponseDto(
            member.getMemberId(),
            member.getEmail(),
            member.getNickname()
        );
        return new RsData<>("200", "로그인에 성공했습니다.", response);
    }

    // JWT 토큰을 별도로 생성하는 메서드
    public String generateAccessToken(Member member) {
        return authTokenService.genAccessToken(member);
    }

    @Transactional
    public RsData<MemberJoinResponseDto> edit(MemberEditRequestDto request){
        // 현재 로그인한 사용자 조회
        Member member = memberRepository.findByMemberId(requestContext.getCurrentMemberId())
            .orElseThrow(() -> new ServiceException("404", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND));

        //  현재 비밀번호 재확인
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new ServiceException("401", "현재 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        // 이메일이나 닉네임이 변경되는지 확인
        boolean emailChanged = !member.getEmail().equals(request.email());
        boolean nicknameChanged = !member.getNickname().equals(request.nickname());

        // 이메일 중복 체크 (현재 사용자와 다른 이메일인 경우)
        if (emailChanged) {
            if (memberRepository.findByEmail(request.email()).isPresent()) {
                throw new ServiceException("409", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT);
            }
        }

        // 닉네임 중복 체크 (현재 사용자와 다른 닉네임인 경우)
        if (nicknameChanged) {
            if (memberRepository.findByNickname(request.nickname()).isPresent()) {
                throw new ServiceException("409", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT);
            }
        }

        // 새로운 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.newPassword());

        // 회원 정보 업데이트
        member.updateMemberInfo(
            request.email(),
            encodedPassword,
            request.nickname(),
            request.profileImage()
        );

        // 데이터베이스에 저장
        Member updatedMember = memberRepository.save(member);

        //토큰 정보가 변경된 경우 새로운 토큰 발급
        if (emailChanged || nicknameChanged) {
            String newAccessToken = authTokenService.genAccessToken(updatedMember);
            requestContext.setCookie("accessToken", newAccessToken);
        }

        // 응답 DTO 생성
        MemberJoinResponseDto response = MemberJoinResponseDto.from(updatedMember);
        return new RsData<>("200", "회원정보 수정에 성공했습니다.", response);
    }

}
