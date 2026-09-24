package com.attendance.attendance_management.services;

import com.attendance.attendance_management.dto.UserDto;
import com.attendance.attendance_management.mapper.UserMapper;
import com.attendance.attendance_management.repository.UserRepository;
import com.attendance.attendance_management.table.UserInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElse(null);
    }

    public List<UserDto> getUsersByRoll(String roll) {
        return userRepository.findByRoll(roll)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public String softDeleteUser(Long id) {
        Optional<UserInfo> userInfo = userRepository.findById(id);
        if (userInfo.isPresent() && userInfo.get().isActive()) {
            userRepository.softDelete(id);
            return "User deactivated successfully";
        }
        return "No active user found with the given ID";
    }

    public List<UserDto> getUnmarkedUsers() {
        return userRepository.findByIsMarked(false)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public String addUser(UserDto userDto) {
        long count = userRepository.count();
        if (count > 0) {
            if (userDto.getUserId() != null && userRepository.existsById(userDto.getUserId())) {
                throw new RuntimeException("User already exists!");
            }
        }
        UserInfo userInfo = userMapper.toEntity(userDto);
         userRepository.save(userInfo);
         return "User added";
    }

}
