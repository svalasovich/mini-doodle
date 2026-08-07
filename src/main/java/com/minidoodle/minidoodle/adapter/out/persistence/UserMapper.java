package com.minidoodle.minidoodle.adapter.out.persistence;

import com.minidoodle.minidoodle.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public UserEntity toEntity(User user) {
        return new UserEntity(user.id(), user.email());
    }

    public User toModel(UserEntity entity) {
        return new User(entity.getId(), entity.getEmail());
    }
}
