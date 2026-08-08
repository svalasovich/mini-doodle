package com.minidoodle.minidoodle.adapter.in.api;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@ValidTimeRange
public record SlotRangeRequest(@NotNull Instant start, @NotNull Instant end) {}
