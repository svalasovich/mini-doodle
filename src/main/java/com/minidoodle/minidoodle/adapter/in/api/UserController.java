package com.minidoodle.minidoodle.adapter.in.api;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.port.in.UserCreateUseCase;
import com.minidoodle.minidoodle.port.in.UserGetUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCreateUseCase userCreateUseCase;
    private final UserGetUseCase userGetUseCase;
    private final UserRequestMapper userRequestMapper;

    @PostMapping
    public @ResponseBody ResponseEntity<User> create(@RequestBody @Valid UserCreateRequest request) {
        var user = userCreateUseCase.create(userRequestMapper.toUserCommand(request));
        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/{id}")
    public @ResponseBody ResponseEntity<User> get(@PathVariable Long id) {
        return ResponseEntity.of(userGetUseCase.get(id));
    }
}
