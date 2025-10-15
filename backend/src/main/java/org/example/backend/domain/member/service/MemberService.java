package org.example.backend.domain.member.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.dto.MemberJoinRequestDto;
import org.example.backend.domain.member.dto.MemberJoinResponseDto;
import org.example.backend.domain.member.dto.MemberLoginRequestDto;
import org.example.backend.domain.member.dto.MemberLoginResponseDto;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.global.exception.ServiceException;
import org.example.backend.global.rsdata.RsData;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;;

    @Transactional
    public RsData<MemberJoinResponseDto> join(MemberJoinRequestDto request) {
        Optional<Member> optionalMember = memberRepository.findByEmail(request.email());
        // 이메일 중복 체크
        if (optionalMember.isPresent()) {
            throw new ServiceException("409", "이미 사용 중인 이메일입니다.",HttpStatus.CONFLICT);
        }

        optionalMember = memberRepository.findByNickname(request.nickname());
        // 닉네임 중복 체크
        if (optionalMember.isPresent()) {
            throw new ServiceException("409", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 회원 생성
        Member member = Member.builder()
                .email(request.email())
                .password(encodedPassword)
                .nickname(request.nickname())
                .profileImage(request.profileImage())
                .build();

        Member savedMember = memberRepository.save(member);

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
}
