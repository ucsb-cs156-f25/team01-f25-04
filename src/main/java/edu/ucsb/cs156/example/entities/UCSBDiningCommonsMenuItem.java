package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is a JPA entity that represents a UCSBDiningCommons
 *
 * <p>A UCSBDiningCommons is a dining commons at UCSB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "UCSBDiningCommonsMenuItem")
public class UCSBDiningCommonsMenuItem {
  // @Id private String code;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String diningCommonsCode;
  private String name;
  private String station;
}
