package com.example.car.sharing.service.impl;

import com.example.car.sharing.dto.user.UserDto;
import com.example.car.sharing.exception.EntityNotFoundException;
import com.example.car.sharing.mapper.UserMapper;
import com.example.car.sharing.model.User;
import com.example.car.sharing.repository.UserRepository;
import com.example.car.sharing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public void updateUserRoleById(Long id, User.UserRole role) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can`t find user by id: " + id));
        existingUser.setRole(role);
        userRepository.save(existingUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can`t find user by id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can`t find user by id: " + id));
        User updatedUser = userMapper.toModel(userDto);
        updatedUser.setId(existingUser.getId());
        return userMapper.toDto(userRepository.save(updatedUser));
    }
}