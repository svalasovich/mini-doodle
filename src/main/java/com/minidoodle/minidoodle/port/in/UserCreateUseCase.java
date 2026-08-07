package com.minidoodle.minidoodle.port.in;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.domain.model.UserCreateCommand;

public interface UserCreateUseCase {
    User create(UserCreateCommand command);
}
