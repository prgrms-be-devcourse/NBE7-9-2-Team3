package org.example.backend.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.dto.MemberEditRequestDto;
import org.example.backend.domain.member.dto.MemberEditResponseDto;
import org.example.backend.domain.member.dto.MemberJoinRequestDto;
import org.example.backend.domain.member.dto.MemberJoinResponseDto;
import org.example.backend.domain.member.dto.MemberLoginRequestDto;
import org.example.backend.domain.member.dto.MemberLoginResponseDto;
import org.example.backend.domain.member.dto.MemberResponseDto;
import org.example.backend.domain.member.service.MemberService;
import org.example.backend.global.exception.ServiceException;
import org.example.backend.global.requestcontext.RequestContext;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final RequestContext requestContext;

    @PostMapping("/join")
    public ApiResponse<MemberJoinResponseDto> join(
        @Valid @ModelAttribute MemberJoinRequestDto request,
        @RequestPart(required = false) MultipartFile profileImageFile) {
        return memberService.join(request, profileImageFile);
    }

    @PostMapping("/login")
    public ApiResponse<MemberLoginResponseDto> login(@Valid @RequestBody MemberLoginRequestDto request) {
        ApiResponse<MemberLoginResponseDto> result = memberService.login(request);
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
    public ApiResponse<MemberLoginResponseDto> logout() {
        requestContext.deleteCookie("accessToken");
        return new ApiResponse<>("200", "로그아웃에 성공했습니다.", null);
    }

    @PutMapping("/me")
    public ApiResponse<MemberEditResponseDto> edit(
        @Valid @RequestBody MemberEditRequestDto request,
        @RequestPart(required = false) MultipartFile profileImage) {
        ApiResponse<MemberEditResponseDto> result = memberService.edit(request, profileImage);
        
        // 새로운 토큰이 있는 경우 쿠키 업데이트
        if (result.getData() != null && result.getData().newAccessToken() != null) {
            requestContext.setCookie("accessToken", result.getData().newAccessToken());
        }
        
        return result;
    }

    @GetMapping("/me")
    public ApiResponse<MemberResponseDto> myPage(){
        return memberService.myPage();
    }


}
