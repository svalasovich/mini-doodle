package com.minidoodle.minidoodle.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.minidoodle.minidoodle.domain.model.User;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserEntityMapperTest {

  private final UserEntityMapper userEntityMapper = new UserEntityMapper();

  @Test
  void toEntity_mapsAllFields() {
    // given
    var createdAt = Instant.parse("2026-08-08T00:00:00Z");
    var user = new User(1L, "Ada Lovelace", "ada@example.com", createdAt);

    // when
    var result = userEntityMapper.toEntity(user);

    // then
    assertThat(result.getId()).isEqualTo(user.id());
    assertThat(result.getName()).isEqualTo(user.name());
    assertThat(result.getEmail()).isEqualTo(user.email());
    assertThat(result.getCreatedAt()).isEqualTo(user.createdAt());
  }

  @Test
  void toModel_mapsAllFields() {
    // given
    var createdAt = Instant.parse("2026-08-08T00:00:00Z");
    var entity = new UserEntity(1L, "Grace Hopper", "grace@example.com", createdAt);

    // when
    var result = userEntityMapper.toModel(entity);

    // then
    assertThat(result.id()).isEqualTo(entity.getId());
    assertThat(result.name()).isEqualTo(entity.getName());
    assertThat(result.email()).isEqualTo(entity.getEmail());
    assertThat(result.createdAt()).isEqualTo(entity.getCreatedAt());
  }
}
