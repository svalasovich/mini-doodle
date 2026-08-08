package com.minidoodle.minidoodle.adapter.in.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.minidoodle.minidoodle.domain.model.User;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserControllerMapperTest {

  private final UserControllerMapper userControllerMapper = new UserControllerMapper();

  @Test
  void toUserCommand_mapsNameAndEmail() {
    // given
    var request = new UserCreateRequest("Ada Lovelace", "ada@example.com");

    // when
    var result = userControllerMapper.toUserCommand(request);

    // then
    assertThat(result.name()).isEqualTo(request.name());
    assertThat(result.email()).isEqualTo(request.email());
  }

  @Test
  void toUserResponse_mapsAllFields() {
    // given
    var createdAt = Instant.parse("2026-08-08T00:00:00Z");
    var user = new User(1L, "Grace Hopper", "grace@example.com", createdAt);

    // when
    var result = userControllerMapper.toUserResponse(user);

    // then
    assertThat(result.id()).isEqualTo(user.id());
    assertThat(result.name()).isEqualTo(user.name());
    assertThat(result.email()).isEqualTo(user.email());
    assertThat(result.createdAt()).isEqualTo(user.createdAt());
  }
}
