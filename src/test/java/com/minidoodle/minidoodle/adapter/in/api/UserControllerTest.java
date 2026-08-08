package com.minidoodle.minidoodle.adapter.in.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.port.in.UserCreateUseCase;
import com.minidoodle.minidoodle.port.in.UserGetUseCase;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@Import(UserControllerMapper.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserCreateUseCase userCreateUseCase;
  @MockitoBean private UserGetUseCase userGetUseCase;

  @Test
  void create_returns200WithCreatedUser() throws Exception {
    // given
    var request = new UserCreateRequest("Ada Lovelace", "ada@example.com");
    var created =
        new User(1L, request.name(), request.email(), Instant.parse("2026-08-08T00:00:00Z"));
    when(userCreateUseCase.create(any())).thenReturn(created);

    // when
    var result =
        mockMvc.perform(
            post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(created.id().intValue())))
        .andExpect(jsonPath("$.name", is(created.name())))
        .andExpect(jsonPath("$.email", is(created.email())));
  }

  @Test
  void create_returns400WhenEmailInvalid() throws Exception {
    // given
    var request = new UserCreateRequest("Ada Lovelace", "not-an-email");

    // when
    var result =
        mockMvc.perform(
            post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

    // then
    result.andExpect(status().isBadRequest());
  }

  @Test
  void get_returns200WhenFound() throws Exception {
    // given
    var userId = 1L;
    var user =
        new User(
            userId, "Grace Hopper", "grace@example.com", Instant.parse("2026-08-08T00:00:00Z"));
    when(userGetUseCase.get(userId)).thenReturn(Optional.of(user));

    // when
    var result = mockMvc.perform(get("/v1/users/" + userId));

    // then
    result.andExpect(status().isOk()).andExpect(jsonPath("$.email", is(user.email())));
  }

  @Test
  void get_returns404WhenNotFound() throws Exception {
    // given
    var userId = 99L;
    when(userGetUseCase.get(userId)).thenReturn(Optional.empty());

    // when
    var result = mockMvc.perform(get("/v1/users/" + userId));

    // then
    result.andExpect(status().isNotFound());
  }
}
