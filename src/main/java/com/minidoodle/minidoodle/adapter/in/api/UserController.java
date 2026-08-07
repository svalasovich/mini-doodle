package com.minidoodle.minidoodle.adapter.in.api;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.port.in.UserCreateUseCase;
import com.minidoodle.minidoodle.port.in.UserGetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController("v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCreateUseCase userCreateUseCase;
    private final UserGetUseCase userGetUseCase;

    @PostMapping
    public @ResponseBody Object create(@RequestBody User user) {
        return null;
    }

    @GetMapping("/{id}")
    public @ResponseBody Optional<User> get(@PathVariable Long id) {
        return userGetUseCase.get(id);
    }
}
