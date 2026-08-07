package com.minidoodle.minidoodle.domain.model;

import java.time.Instant;

public record User(Long id, String name, String email, Instant createdAt) {
}
