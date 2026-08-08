package com.minidoodle.minidoodle.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "slots")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column private Long meetingId;

  @Column(nullable = false)
  private Instant startTime;

  @Column(nullable = false)
  private Instant endTime;

  @CreationTimestamp(source = SourceType.DB)
  private Instant createdAt;

  @UpdateTimestamp(source = SourceType.DB)
  private Instant updatedAt;
}
