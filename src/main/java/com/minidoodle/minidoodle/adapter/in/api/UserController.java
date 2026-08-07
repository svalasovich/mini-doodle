package com.minidoodle.minidoodle.adapter.in.api;

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
    private final UserControllerMapper userControllerMapper;

    @PostMapping
    public @ResponseBody ResponseEntity<UserResponse> create(@RequestBody @Valid UserCreateRequest request) {
        var user = userCreateUseCase.create(userControllerMapper.toUserCommand(request));
        var response = userControllerMapper.toUserResponse(user);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public @ResponseBody ResponseEntity<UserResponse> get(@PathVariable Long id) {
        var response = userGetUseCase.get(id).map(userControllerMapper::toUserResponse);

        return ResponseEntity.of(response);
    }
}
