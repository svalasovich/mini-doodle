package com.minidoodle.minidoodle.adapter.out.persistence;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.port.out.UserCreatePort;
import com.minidoodle.minidoodle.port.out.UserGetPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAdapter implements UserCreatePort, UserGetPort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> get(Long id) {
        return userRepository.findById(id).map(userMapper::toModel);
    }

    @Override
    public User create(User user) {
        var entity = userRepository.save(userMapper.toEntity(user));
        return userMapper.toModel(entity);
    }
}
