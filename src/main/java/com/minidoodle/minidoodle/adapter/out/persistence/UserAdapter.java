package com.minidoodle.minidoodle.adapter.out.persistence;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.port.out.UserCreatePort;
import com.minidoodle.minidoodle.port.out.UserGetPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAdapter implements UserCreatePort, UserGetPort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> get(Long id) {
        return userRepository.findById(id).map(userMapper::toModel);
    }
}
