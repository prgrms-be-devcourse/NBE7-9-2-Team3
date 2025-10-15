package org.example.backend.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.dto.MemberJoinRequestDto;
import org.example.backend.domain.member.dto.MemberJoinResponseDto;
import org.example.backend.domain.member.dto.MemberLoginRequestDto;
import org.example.backend.domain.member.dto.MemberLoginResponseDto;
import org.example.backend.domain.member.service.MemberService;
import org.example.backend.global.rsdata.RsData;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

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
                memberService.findByEmail(request.email()).orElseThrow(null)
            );
            
            // HttpOnly 쿠키로 토큰 설정
            Cookie cookie = new Cookie("accessToken", accessToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // HTTPS에서만 전송
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            response.addCookie(cookie);
        }
        
        return result;
    }

}
