package com.minidoodle.minidoodle.adapter.out.persistence;

import com.minidoodle.minidoodle.domain.model.User;
import com.minidoodle.minidoodle.port.out.UserCreatePort;
import com.minidoodle.minidoodle.port.out.UserGetPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserAdapter implements UserCreatePort, UserGetPort {

  private final UserRepository userRepository;
  private final UserEntityMapper userEntityMapper;

  @Override
  public Optional<User> get(Long id) {
    return userRepository.findById(id).map(userEntityMapper::toModel);
  }

  @Override
  public User create(User user) {
    var entity = userRepository.save(userEntityMapper.toEntity(user));
    return userEntityMapper.toModel(entity);
  }
}
