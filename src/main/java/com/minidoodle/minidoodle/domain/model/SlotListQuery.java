package com.minidoodle.minidoodle.domain.model;

import java.time.Instant;

public record SlotListQuery(Long userId, Instant from, Instant to, Boolean available) {}
