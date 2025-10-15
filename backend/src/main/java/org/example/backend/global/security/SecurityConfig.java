package org.example.backend.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 설정을 활성화
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. CSRF 비활성화 (API 서버의 경우 일반적으로 비활성화)
        http.csrf(AbstractHttpConfigurer::disable);

        // 2. H2 Console을 위한 설정
        http.headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // H2 Console이 iframe을 사용하므로 허용
        );

        // 3. 인증/인가 설정 (모두 허용)
        http.authorizeHttpRequests(authorize -> authorize
                // H2 Console 경로 허용
                .requestMatchers("/h2-console/**").permitAll()
                // 모든 경로 ("/**")에 대한 요청을 인증/인가 없이 허용합니다.
                .requestMatchers("/**").permitAll()
                // 위 설정을 하지 않은 나머지 요청은 인증을 요구하도록 설정
                .anyRequest().authenticated()
        );

        // 4. 폼 로그인 비활성화 (API 서버의 경우)
        http.formLogin(AbstractHttpConfigurer::disable);

        // 5. HTTP Basic 인증 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
