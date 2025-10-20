package org.example.backend.global.security;

import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.exception.ServiceException;
import org.example.backend.global.requestcontext.RequestContext;
import org.example.backend.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenService authTokenService;
    private final MemberRepository memberRepository;
    private final RequestContext rq;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("CustomAuthenticationFilter called");

        try {
            authenticate(request, response, filterChain);
        } catch (ServiceException e) {
            ApiResponse<?> rsData = e.getRsData();
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(rsData.getStatusCode());
            response.getWriter().write("""
                    {
                        "resultCode": "%s",
                        "msg": "%s"
                    }
                    """.formatted(rsData.getResultCode(), rsData.getMsg()));
        } catch (Exception e) {
            throw e;
        }
    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if(List.of("/api/members/join", "/api/members/login").contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken;
        String headerAuthorization = rq.getHeader("Authorization", "");

        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer "))
                throw new ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.", HttpStatus.UNAUTHORIZED);

            accessToken = headerAuthorization.substring(7);
        } else {
            accessToken = rq.getCookieValue("accessToken", "");
        }

        if (accessToken == null || accessToken.isBlank()) {
            throw new ServiceException("401-1", "Access Token이 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> payload = authTokenService.payloadOrNull(accessToken);

        if (payload == null) {
            throw new ServiceException("401-3", "Access Token이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        long id = (long) payload.get("id");

        // 데이터베이스에서 실제 Member 조회
        Optional<Member> optionalMember = memberRepository.findById(id);
        if (optionalMember.isEmpty()) {
            throw new ServiceException("404", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND);
        }

        Member member = optionalMember.get();

        UserDetails user = new CustomUserDetails(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user,
            user.getPassword(),
            user.getAuthorities()
        );

        SecurityContextHolder
            .getContext()
            .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}