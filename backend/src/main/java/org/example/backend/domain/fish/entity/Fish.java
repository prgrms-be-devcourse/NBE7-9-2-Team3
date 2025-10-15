package org.example.backend.domain.fish.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
public class Fish {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long fishId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "aquarium_id")
  private Aquarium aquarium;

  @Column(length = 50)
  private String species;

  @Column(length = 50)
  private String name;

  @CreatedDate
  private LocalDateTime createDate;

  public void changeAquarium(Aquarium myOwnedAquarium) {
    this.aquarium = myOwnedAquarium;
  }
}
