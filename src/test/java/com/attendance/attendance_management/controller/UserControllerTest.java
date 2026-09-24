package com.attendance.attendance_management.controller;

import com.attendance.attendance_management.dto.UserDto;
import com.attendance.attendance_management.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;
    UserDto userDto;


    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "john", "teacher", "cse","true");
    }

    @Test
    void getUser() {
        List<UserDto> userDtoList = new ArrayList<>();
        userDtoList.add(userDto);
        when(userService.getAllUsers()).thenReturn(userDtoList);
        assertEquals(userDtoList, userController.getAllUsers());

    }

    @Test
    void getUserById() {
        when(this.userService.getUserById(1L)).thenReturn(this.userDto);
        assertEquals(this.userDto, this.userController.getUserById(1L).getBody());
    }

    @Test
    void getUserByRoll() {
        List<UserDto> userDtoList = new ArrayList<>();
        userDtoList.add(this.userDto);
        when(this.userService.getUsersByRoll("teacher")).thenReturn(userDtoList);
        assertEquals(userDtoList, this.userController.getUsersByRoll("teacher"));
    }

    @Test
    void getUserByDepartment() {
        List<UserDto> userDtoList = new ArrayList<>();
        userDtoList.add(this.userDto);
        when(this.userService.getUsersByDepartment("cse")).thenReturn(userDtoList);
        assertEquals(userDtoList, this.userController.getUsersByDepartment("cse"));
    }

//    @Test
//    void getDelete() {
//        when(this.userService.getDe(1L)).thenReturn("Deleted");
//        assertEquals("Deleted", this.userController.getDelete("1"));
//
//    }

}