package com.minidoodle.minidoodle.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.minidoodle.minidoodle.domain.model.UserCreateCommand;
import org.junit.jupiter.api.Test;

class UserMapperTest {

  private final UserMapper userMapper = new UserMapper();

  @Test
  void toUser_mapsNameAndEmailAndLeavesIdAndCreatedAtNull() {
    // given
    var command = new UserCreateCommand("Ada Lovelace", "ada@example.com");

    // when
    var result = userMapper.toUser(command);

    // then
    assertThat(result.id()).isNull();
    assertThat(result.name()).isEqualTo(command.name());
    assertThat(result.email()).isEqualTo(command.email());
    assertThat(result.createdAt()).isNull();
  }
}
