package org.example.backend.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.dto.MemberJoinRequestDto;
import org.example.backend.domain.member.dto.MemberJoinResponseDto;
import org.example.backend.domain.member.service.MemberService;
import org.example.backend.global.rsdata.RsData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public RsData<MemberJoinResponseDto> join(@Valid @RequestBody MemberJoinRequestDto request) {
        return memberService.join(request);
    }
}
