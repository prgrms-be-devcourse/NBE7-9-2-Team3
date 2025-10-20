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
    String password,

    @NotBlank
    String nickname
) {}
