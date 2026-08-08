package com.minidoodle.minidoodle.port.out;

import com.minidoodle.minidoodle.domain.model.User;

public interface UserCreatePort {

  User create(User user);
}
