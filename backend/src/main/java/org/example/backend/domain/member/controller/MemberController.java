package org.example.backend.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.dto.MemberEditRequestDto;
import org.example.backend.domain.member.dto.MemberEditResponseDto;
import org.example.backend.domain.member.dto.MemberJoinRequestDto;
import org.example.backend.domain.member.dto.MemberJoinResponseDto;
import org.example.backend.domain.member.dto.MemberLoginRequestDto;
import org.example.backend.domain.member.dto.MemberLoginResponseDto;
import org.example.backend.domain.member.dto.MemberResponseDto;
import org.example.backend.domain.member.dto.MemberSearchListResponseDto;
import org.example.backend.domain.member.service.MemberService;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.requestcontext.RequestContext;
import org.example.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관리 API")
public class MemberController {

    private final MemberService memberService;
    private final RequestContext requestContext;

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @PostMapping("/join")
    public ApiResponse<MemberJoinResponseDto> join(
        @Parameter(description = "회원가입 정보", required = true)
        @Valid @ModelAttribute MemberJoinRequestDto request,
        @Parameter(description = "프로필 이미지 파일 (선택사항)")
        @RequestPart(required = false) MultipartFile profileImageFile) {
        return memberService.join(request, profileImageFile);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ApiResponse<MemberLoginResponseDto> login(
        @Parameter(description = "로그인 정보", required = true)
        @Valid @RequestBody MemberLoginRequestDto request) {
        ApiResponse<MemberLoginResponseDto> result = memberService.login(request);
        // JWT 토큰을 HttpOnly 쿠키로 설정
        if (result.getData() != null) {
            // 로그인한 사용자 정보로 토큰 생성
            String accessToken = memberService.generateAccessToken(
                memberService.findByEmail(request.email()).orElseThrow(
                    ()->new BusinessException(ErrorCode.MEMBER_NOT_FOUND)));
            requestContext.setCookie("accessToken", accessToken);
        }
        return result;
    }
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        requestContext.deleteCookie("accessToken");
        return ApiResponse.ok("로그아웃에 성공했습니다.");
    }

    @Operation(summary = "회원정보 수정", description = "현재 로그인된 사용자의 정보를 수정합니다.")
    @PutMapping("/me")
    public ApiResponse<MemberEditResponseDto> edit(
        @Parameter(description = "회원정보 수정 데이터", required = true)
        @Valid @ModelAttribute MemberEditRequestDto request) {
        ApiResponse<MemberEditResponseDto> result = memberService.edit(request, null);
        
        // 새로운 토큰이 있는 경우 쿠키 업데이트
        if (result.getData() != null && result.getData().newAccessToken() != null) {
            requestContext.setCookie("accessToken", result.getData().newAccessToken());
        }
        
        return result;
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<MemberResponseDto> myPage(){
        return memberService.myPage();
    }

    @Operation(summary = "프로필 이미지 수정", description = "현재 로그인된 사용자의 프로필 이미지를 수정합니다.")
    @PutMapping("/me/profile-image")
    public ApiResponse<MemberEditResponseDto> updateProfileImage(
        @Parameter(description = "프로필 이미지 파일", required = true)
        @RequestPart MultipartFile profileImage) {
        ApiResponse<MemberEditResponseDto> result = memberService.updateProfileImage(profileImage);
        
        // 새로운 토큰이 있는 경우 쿠키 업데이트
        if (result.getData() != null && result.getData().newAccessToken() != null) {
            requestContext.setCookie("accessToken", result.getData().newAccessToken());
        }
        
        return result;
    }

    @Operation(summary = "회원 검색", description = "닉네임으로 회원을 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<MemberSearchListResponseDto> searchMembers(
        @Parameter(description = "검색할 닉네임", required = true)
        @RequestParam String nickname) {
        return memberService.searchMembers(nickname);
    }

}
