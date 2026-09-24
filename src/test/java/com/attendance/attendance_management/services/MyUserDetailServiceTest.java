package com.attendance.attendance_management.services;

import com.attendance.attendance_management.exceptionhandler.customexceptions.UserNotFoundException;
import com.attendance.attendance_management.repository.UserAuthRepository;
import com.attendance.attendance_management.table.UserAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyUserDetailServiceTest {

    @Mock
    private UserAuthRepository userAuthRepository;

    @InjectMocks
    private MyUserDetailService myUserDetailService;

    private UserAuth userAuth;

    @BeforeEach
    void setUp() {
        userAuth = new UserAuth();
    }

    @Test
    void loadUserByUsername_userFound() {

        String userName = "john";
        this.userAuth.setUserName(userName);
        when(this.userAuthRepository.findByUserName(userName)).thenReturn(this.userAuth);
        UserDetails userDetails = this.myUserDetailService.loadUserByUsername(userName);

        assertNotNull(userDetails);
        assertEquals(this.userAuth.getUsername(), userDetails.getUsername());
        verify(this.userAuthRepository, times(1)).findByUserName(userName);
    }

    @Test
    void loadUserByUsername_userNotFound() {
        String username = "nonExistentUser";
        when(this.userAuthRepository.findByUserName(username)).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> {
            this.myUserDetailService.loadUserByUsername(username);
        });
        verify(this.userAuthRepository, times(1)).findByUserName(username);
    }
}
