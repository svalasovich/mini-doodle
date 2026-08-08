package com.minidoodle.minidoodle.adapter.in.api;

import java.time.Instant;

public record SlotResponse(
    Long id, Long userId, Instant startTime, Instant endTime, boolean available) {}
