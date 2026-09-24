package com.attendance.attendance_management.services;

import com.attendance.attendance_management.dto.UserDto;
import com.attendance.attendance_management.mapper.UserMapper;
import com.attendance.attendance_management.repository.UserRepository;
import com.attendance.attendance_management.table.UserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;


    @Test
    void TestGetUser() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(20L);
        userInfo.setName("Michael Johnson");
        userInfo.setRoll("teacher");
        List<UserInfo> userInfoList = new ArrayList<>();
        userInfoList.add(userInfo);

        UserDto userDto = new UserDto();
        userDto.setUserId(20L);
        userDto.setName("Michael Johnson");
        userDto.setRoll("teacher");
        List<UserDto> UserDtoList = new ArrayList<>();
        UserDtoList.add(userDto);

        when(this.userRepository.findAll()).thenReturn(userInfoList);
        when(this.userMapper.toDto(userInfo)).thenReturn(userDto);
        List<UserDto> actualUserDtoList = this.userService.getAllUsers();
        assertEquals(UserDtoList.size(), actualUserDtoList.size());
        assertEquals(UserDtoList.getFirst().getUserId(), actualUserDtoList.getFirst().getUserId());
        assertEquals(UserDtoList.getFirst().getName(), actualUserDtoList.getFirst().getName());
        assertEquals(UserDtoList.getFirst().getRoll(), actualUserDtoList.getFirst().getRoll());
        verify(this.userRepository, times(1)).findAll();
    }

    @Test
    void TestGetUserById() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(20L);
        userInfo.setName("Michael Johnson");

        UserDto userDto = new UserDto();
        userDto.setUserId(20L);
        userDto.setName("Michael Johnson");

        when(this.userRepository.findById(20L)).thenReturn(Optional.of(userInfo));
        when(this.userMapper.toDto(userInfo)).thenReturn(userDto);
        assertEquals("Michael Johnson", this.userService.getUserById(20L).getName());
        verify(this.userRepository, times(1)).findById(20L);
        verify(this.userMapper, times(1)).toDto(userInfo);
    }

//    @Test
//    void TestIfNotGetUserById()
//    {
//        when(userRepository.findById(3L)).thenReturn(Optional.empty());
//        UserDto result= userService.getUserById(3L);
//        assertNull(result);
//        verify(userRepository, times(1)).findById(3L);
//    }


//    @Test
//    void TestNoDeleteById() {
//        UserInfo userAuth = new UserInfo(2L, "mano",
//                "teacher", "cse", true);
//        when(this.userRepository.findById(2L)).thenReturn(Optional.empty());
//        String result = this.userService.getDelete(userAuth.getUserId());
//        assertEquals("No match found", result);
//        verify(this.userRepository, times(1)).findById(2L);
//    }

    @Test
    void TestGetUserByRoll() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(20L);
        userInfo.setName("Michael Johnson");
        userInfo.setRoll("teacher");

        UserDto userDto = new UserDto();
        userDto.setUserId(20L);
        userDto.setName("Michael Johnson");
        userDto.setRoll("teacher");

        List<UserInfo> userInfoList = new ArrayList<>();
        userInfoList.add(userInfo);
        when(this.userRepository.findByRoll("teacher")).thenReturn(userInfoList);
        when(this.userMapper.toDto(userInfo)).thenReturn(userDto);
        List<UserDto> result = this.userService.getUsersByRoll("teacher");

        assertEquals(1, result.size());
        assertEquals(userDto, result.getFirst());
        verify(this.userRepository, times(1)).findByRoll("teacher");
        verify(this.userMapper, times(1)).toDto(userInfo);
    }


    @Test
    void TestGetUserByDepartment() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(20L);
        userInfo.setName("Michael Johnson");
        userInfo.setRoll("teacher");
        userInfo.setDepartment("cse");

        UserDto userDto = new UserDto();
        userDto.setUserId(20L);
        userDto.setName("Michael Johnson");
        userDto.setRoll("teacher");
        userDto.setDepartment("cse");

        List<UserInfo> userInfoList = new ArrayList<>();
        userInfoList.add(userInfo);
        when(this.userRepository.findByDepartment("cse")).thenReturn(userInfoList);
        when(this.userMapper.toDto(userInfo)).thenReturn(userDto);
        List<UserDto> result = this.userService.getUsersByDepartment("cse");

        assertEquals(1, result.size());
        assertEquals(userDto, result.getFirst());
        verify(this.userRepository, times(1)).findByDepartment("cse");
        verify(this.userMapper, times(1)).toDto(userInfo);
    }

}