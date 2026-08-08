package com.minidoodle.minidoodle.port.in;

import com.minidoodle.minidoodle.domain.model.User;
import java.util.Optional;

public interface UserGetUseCase {
  Optional<User> get(Long id);
}
