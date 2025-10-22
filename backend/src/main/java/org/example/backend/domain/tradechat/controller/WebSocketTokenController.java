package org.example.backend.domain.tradechat.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class WebSocketTokenController {

    private final AuthTokenService authTokenService;
    private final MemberRepository memberRepository;

    @Operation(summary = "웹소켓 연결 전 토큰 인증", description = "웹소켓 연결 전 토큰을 세션에 저장합니다.")
    @GetMapping("/ws-token")
    public ResponseEntity<Map<String, String>> issueWebSocketToken(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // 토큰 받아오기
        String wsToken = authTokenService.genAccessToken(member);

        Map<String, String> response = Map.of("accessToken", wsToken);
        return ResponseEntity.ok(response);
    }
}
