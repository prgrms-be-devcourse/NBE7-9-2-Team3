package org.example.backend.domain.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.dto.MemberEditRequestDto;
import org.example.backend.domain.member.dto.MemberJoinRequestDto;
import org.example.backend.domain.member.dto.MemberJoinResponseDto;
import org.example.backend.domain.member.dto.MemberLoginRequestDto;
import org.example.backend.domain.member.dto.MemberLoginResponseDto;
import org.example.backend.domain.member.service.MemberService;
import org.example.backend.global.exception.ServiceException;
import org.example.backend.global.requestcontext.RequestContext;
import org.example.backend.global.rsdata.RsData;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final RequestContext requestContext;

    @PostMapping("/join")
    public RsData<MemberJoinResponseDto> join(@Valid @RequestBody MemberJoinRequestDto request) {
        return memberService.join(request);
    }

    @PostMapping("/login")
    public RsData<MemberLoginResponseDto> login(@Valid @RequestBody MemberLoginRequestDto request, HttpServletResponse response) {
        RsData<MemberLoginResponseDto> result = memberService.login(request);
        // JWT 토큰을 HttpOnly 쿠키로 설정
        if (result.getData() != null) {
            // 로그인한 사용자 정보로 토큰 생성
            String accessToken = memberService.generateAccessToken(
                memberService.findByEmail(request.email()).orElseThrow(
                    ()->new ServiceException("404","이메일로 찾을 수 없습니다. Email: " + request.email(), HttpStatus.CONFLICT)));
            requestContext.setCookie("accessToken", accessToken);
        }
        return result;
    }
    @PostMapping("/logout")
    public RsData<MemberLoginResponseDto> logout(@Valid @RequestBody MemberLoginRequestDto request, HttpServletResponse response) {

        requestContext.deleteCookie("accessToken");
        return new RsData<>("200", "로그아웃에 성공했습니다.", null);
    }

    @PutMapping("/me")
    public RsData<MemberJoinResponseDto> edit(@Valid @RequestBody MemberEditRequestDto request) {
        return memberService.edit(request);
    }


}
