package com.minidoodle.minidoodle.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.domain.model.UserCreateCommand;
import com.minidoodle.minidoodle.port.out.UserCreatePort;
import com.minidoodle.minidoodle.port.out.UserGetPort;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserCreatePort userCreatePort;
  @Mock private UserGetPort userGetPort;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userCreatePort, userGetPort, new UserMapper());
  }

  @Test
  void create_mapsCommandAndReturnsWhatThePortReturns() {
    // given
    var command = new UserCreateCommand("Ada Lovelace", "ada@example.com");
    var created = new User(1L, command.name(), command.email(), Instant.now());
    var captor = ArgumentCaptor.forClass(User.class);
    when(userCreatePort.create(captor.capture())).thenReturn(created);

    // when
    var result = userService.create(command);

    // then
    assertThat(result).isEqualTo(created);
    assertThat(captor.getValue().id()).isNull();
    assertThat(captor.getValue().name()).isEqualTo(command.name());
    assertThat(captor.getValue().email()).isEqualTo(command.email());
  }

  @Test
  void get_delegatesToPortAndReturnsItsResult() {
    // given
    var userId = 1L;
    var user = new User(userId, "Grace Hopper", "grace@example.com", Instant.now());
    when(userGetPort.get(userId)).thenReturn(Optional.of(user));

    // when
    var result = userService.get(userId);

    // then
    assertThat(result).contains(user);
  }

  @Test
  void get_returnsEmptyWhenPortFindsNothing() {
    // given
    var userId = 1L;
    when(userGetPort.get(userId)).thenReturn(Optional.empty());

    // when
    var result = userService.get(userId);

    // then
    assertThat(result).isEmpty();
  }
}
