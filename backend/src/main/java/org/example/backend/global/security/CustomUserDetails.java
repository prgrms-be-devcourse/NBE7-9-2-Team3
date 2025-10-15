package org.example.backend.global.security;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.example.backend.domain.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {
  private final Member member;

  public CustomUserDetails(Member member) {
    this.member = member;
  }

  public Long getId() {
    return member.getMemberId();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    return "";
  }

  @Override
  public String getUsername() {
    return "";
  }

}
