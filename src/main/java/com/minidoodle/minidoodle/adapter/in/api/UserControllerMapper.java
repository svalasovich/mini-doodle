package com.minidoodle.minidoodle.adapter.in.api;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.domain.model.UserCreateCommand;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
public class UserControllerMapper {
  public UserCreateCommand toUserCommand(@NotNull UserCreateRequest request) {
    return new UserCreateCommand(request.name(), request.email());
  }

  public UserResponse toUserResponse(User user) {
    return new UserResponse(user.id(), user.name(), user.email(), user.createdAt());
  }
}
