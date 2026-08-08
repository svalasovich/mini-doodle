package com.minidoodle.minidoodle.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.minidoodle.minidoodle.TestcontainersConfiguration;
import com.minidoodle.minidoodle.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, UserAdapter.class, UserEntityMapper.class})
class UserAdapterTest {

  @Autowired private UserAdapter userAdapter;

  @Test
  void create_persistsAndReturnsUserWithGeneratedIdAndCreatedAt() {
    // given
    var user = new User(null, "Ada Lovelace", "ada@example.com", null);

    // when
    var created = userAdapter.create(user);

    // then
    assertThat(created.id()).isNotNull();
    assertThat(created.name()).isEqualTo(user.name());
    assertThat(created.email()).isEqualTo(user.email());
    assertThat(created.createdAt()).isNotNull();
  }

  @Test
  void get_returnsPersistedUser() {
    // given
    var user = new User(null, "Grace Hopper", "grace@example.com", null);
    var created = userAdapter.create(user);

    // when
    var found = userAdapter.get(created.id());

    // then
    assertThat(found).isPresent();
    assertThat(found.get().email()).isEqualTo(user.email());
  }

  @Test
  void get_returnsEmptyWhenNotFound() {
    // when
    var found = userAdapter.get(-1L);

    // then
    assertThat(found).isEmpty();
  }
}
