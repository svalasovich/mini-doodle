package com.minidoodle.minidoodle.adapter.in.api;

import com.minidoodle.minidoodle.domain.model.UserCreateCommand;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
public class UserMapper {
    public UserCreateCommand toUserCommand(@NotNull UserCreateRequest request) {
        return new UserCreateCommand(request.name(), request.email());
    }
}
