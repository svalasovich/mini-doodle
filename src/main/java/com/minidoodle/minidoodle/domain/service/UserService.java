package com.minidoodle.minidoodle.domain.service;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.port.in.UserCreateUseCase;
import com.minidoodle.minidoodle.port.in.UserGetUseCase;
import com.minidoodle.minidoodle.port.out.UserCreatePort;
import com.minidoodle.minidoodle.port.out.UserGetPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserGetUseCase, UserCreateUseCase {

    private final UserCreatePort userCreatePort;
    private final UserGetPort userGetPort;

    @Override
    public void create() {

    }

    @Override
    public Optional<User> get(Long id) {
        return userGetPort.get(id);
    }
}

