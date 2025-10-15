package org.example.backend.domain.member.service;

import java.util.Map;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.global.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    @Value("${custom.jwt.secretPattern}")
    private String secretPattern;
    @Value("${custom.jwt.expireSeconds}")
    private long expireSeconds;

    public String genAccessToken(Member member) {

        return JwtUtil.jwt.toString(
            secretPattern,
            expireSeconds,
            Map.of("id", member.getMemberId(), "email", member.getEmail(), "nickname", member.getNickname())
        );
    }

    public Map<String, Object> payloadOrNull(String jwt) {
        Map<String, Object> payload = JwtUtil.jwt.payloadOrNull(jwt, secretPattern);

        if(payload == null) {
            return null;
        }

        Number idNo = (Number)payload.get("id");
        long id = idNo.longValue();
        String email = (String)payload.get("email");
        String nickname = (String)payload.get("nickname");

        return Map.of("id", id, "email", email, "nickname", nickname);
    }
}