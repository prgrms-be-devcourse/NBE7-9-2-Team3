package org.example.backend.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberJoinRequestDto(
    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 8, max = 20)
    String password, // 회원가입 요청 시 필수 정보 (누락된 부분)

    @NotBlank
    String nickname,

    String profileImage // 선택 사항일 수 있음
) {}
