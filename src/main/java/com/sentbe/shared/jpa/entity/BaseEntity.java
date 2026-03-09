package com.sentbe.shared.jpa.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import static jakarta.persistence.GenerationType.IDENTITY;

@MappedSuperclass
@Getter
public class BaseEntity {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;
  @CreatedDate
  private LocalDateTime createdAt;
  @LastModifiedDate
  private LocalDateTime updatedAt;
}
