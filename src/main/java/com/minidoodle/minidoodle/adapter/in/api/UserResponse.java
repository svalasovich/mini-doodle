package com.minidoodle.minidoodle.adapter.in.api;

import java.time.Instant;

public record UserResponse(Long id, String name, String email, Instant createdAt) {}
