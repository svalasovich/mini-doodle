package com.minidoodle.minidoodle.adapter.out.persistence;

import com.minidoodle.minidoodle.domain.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class UserEntityMapper {

  public UserEntity toEntity(@NotNull User user) {
    return new UserEntity(user.id(), user.name(), user.email(), user.createdAt());
  }

  public User toModel(@NotNull UserEntity entity) {
    return new User(entity.getId(), entity.getName(), entity.getEmail(), entity.getCreatedAt());
  }
}
