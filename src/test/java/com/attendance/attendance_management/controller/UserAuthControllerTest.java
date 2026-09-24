package com.attendance.attendance_management.controller;

import com.attendance.attendance_management.dto.ApiResponse;
import com.attendance.attendance_management.exceptionhandler.customexceptions.InvalidException;
import com.attendance.attendance_management.services.UserAuthService;
import com.attendance.attendance_management.table.UserAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthControllerTest {

    @Mock
    private UserAuthService authService;

    @InjectMocks
    private UserAuthController userAuthController;

    private UserAuth userAuth;


    @BeforeEach
    void setUp() {

//        userAuth = new UserAuth(1L, "john", "j@123", true);

    }

    @Test
    void getRegisterUser() {

        List<UserAuth> userList = new ArrayList<>();
        userList.add(this.userAuth);
        ApiResponse<List<UserAuth>> expectedResponse = new ApiResponse<>(userList, "Success");
        when(this.authService.getRegisterUser()).thenReturn(ResponseEntity.ok(expectedResponse));

        ResponseEntity<ApiResponse<List<UserAuth>>> response = this.userAuthController.getRegisterUser();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void getRegisterById() {
        List<UserAuth> userList = new ArrayList<>();
        userList.add(this.userAuth);
        ApiResponse<UserAuth> message = new ApiResponse<>(userList, "Success");
        when(this.authService.getRegisterById(1L)).thenReturn(ResponseEntity.ok(message));
        ResponseEntity<ApiResponse<UserAuth>> response = this.userAuthController.getRegisterById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(message, response.getBody());

    }

    @Test
    void getRegisterById_invalidId() {
        InvalidException exception = assertThrows(InvalidException.class, () -> {
            this.userAuthController.getRegisterById(-1L);
        });
        assertEquals("input mismatch", exception.getMessage());
    }

    @Test
    void registerUser() {

        String message = "Success";
        when(this.authService.registerUser(this.userAuth)).thenReturn(message);
        String actualMessage = this.userAuthController.registerUser(this.userAuth);
        assertEquals(message, actualMessage);
    }

    @Test
    void login() {

        String message = "Login successful";
        when(this.authService.verifyLogin(this.userAuth)).thenReturn(message);
        String actualMessage = String.valueOf(this.userAuthController.login(this.userAuth));
        assertEquals(message, actualMessage);
    }

    @Test
    void getDelete() {
        String message = "deleted successfully";
        when(this.authService.getDelete(1L)).thenReturn(message);
        String actualMessage = this.userAuthController.getDelete("1");
        assertEquals(message, actualMessage);
    }

}