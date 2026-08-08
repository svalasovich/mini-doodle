package com.minidoodle.minidoodle.domain.service;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.domain.model.UserCreateCommand;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class UserMapper {
  public User toUser(@NotNull UserCreateCommand command) {
    return new User(null, command.name(), command.email(), null);
  }
}
