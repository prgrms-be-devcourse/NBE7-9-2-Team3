package org.example.backend.domain.aquarium.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Aquarium {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long aquariumId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Column(length = 50)
  private String name;

  @CreatedDate
  private LocalDateTime createDate;

  public Aquarium(Member member, String name) {
    this.member = member;
    this.name = name;
  }
}
