package org.example.backend.domain.fish.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.global.jpa.entity.BaseEntity;

@Entity
@Getter
public class Fish extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "aquarium_id")
  private Aquarium aquarium;

  @Column(length = 50)
  private String species;

  @Column(length = 50)
  private String name;

  public void changeAquarium(Aquarium myOwnedAquarium) {
    this.aquarium = myOwnedAquarium;
  }
}
