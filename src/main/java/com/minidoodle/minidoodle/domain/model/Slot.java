package com.minidoodle.minidoodle.domain.model;

import java.time.Instant;

public record Slot(
    Long id,
    Long userId,
    Long meetingId,
    Instant startTime,
    Instant endTime,
    Instant createdAt,
    Instant updatedAt) {

  public boolean isAvailable() {
    return meetingId == null;
  }
}
