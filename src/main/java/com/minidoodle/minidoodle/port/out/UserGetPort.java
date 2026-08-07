package com.minidoodle.minidoodle.port.out;

import com.minidoodle.minidoodle.domain.model.User;

import java.util.Optional;

public interface UserGetPort {
    Optional<User> get(Long id);
}
