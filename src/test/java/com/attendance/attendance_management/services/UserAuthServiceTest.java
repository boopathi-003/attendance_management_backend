package com.attendance.attendance_management.services;

import com.attendance.attendance_management.exceptionhandler.customexceptions.InvalidException;
import com.attendance.attendance_management.exceptionhandler.customexceptions.UserNotFoundException;
import com.attendance.attendance_management.repository.UserAuthRepository;
import com.attendance.attendance_management.table.UserAuth;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtensionMethod(MockitoExtension.class)
class UserAuthServiceTest {

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserAuthService userAuthService;
    private UserAuth userAuth;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userAuth = new UserAuth(2L, "mano",
                "m@123", true, "user");
    }


    @Test
    void TestGetRegisterUser() {
        this.userAuthService.getRegisterUser();
        verify(this.userAuthRepository).findAll();
    }

    @Test
    void TestGetRegisterById() {
        long id = 1L;
        when(this.userAuthRepository.findById(id)).thenReturn(of(this.userAuth));
        String user = Objects.requireNonNull(this.userAuthService.getRegisterById(id).getBody()).getResponse().getUsername();
        assertEquals("mano", user);
        assertEquals("m@123", Objects.requireNonNull(this.userAuthService.getRegisterById(id).getBody()).getResponse().getPassword());
    }

    @Test
    void TestVerifyLogin() {
        when(this.userAuthRepository.findByUserName(this.userAuth.getUsername()))
                .thenReturn(new UserAuth(2L, "mano", "$2a$12$5.bQ.EnTuA..QWU0UgPGAeBhRQCtu4/5bEFwHrM0cs4JFpzcEirzq", true, "user"));

        when(encoder.matches("m@123", "$2a$12$5.bQ.EnTuA..QWU0UgPGAeBhRQCtu4/5bEFwHrM0cs4JFpzcEirzq")).thenReturn(true);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(this.jwtService.getToken("mano", userAuth.getRole())).thenReturn("$2a$12$5.bQ.EnTuA..QWU0UgPGAeBhRQCtu4/5bEFwHrM0cs4JFpzcEirzq");
        String token = this.userAuthService.verifyLogin(this.userAuth);
        assertEquals("$2a$12$5.bQ.EnTuA..QWU0UgPGAeBhRQCtu4/5bEFwHrM0cs4JFpzcEirzq", token);
        verify(this.userAuthRepository, times(1)).findByUserName(this.userAuth.getUsername());
    }

    @Test
    void TestUserNotFound() {
        when(userAuthRepository.findByUserName(userAuth.getUsername())).thenReturn(null);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userAuthService.verifyLogin(userAuth);
        });
        assertEquals("User not found", exception.getMessage());
        verify(userAuthRepository, times(1)).findByUserName(userAuth.getUsername());
    }

    @Test
    void TestUserIsNotActive()
    {
        when(userAuthRepository.findByUserName(userAuth.getUsername()))
                .thenReturn(new UserAuth(20L, "manoj", "m@123", false, "user"));
        InvalidException exception = assertThrows(InvalidException.class, () -> {
            userAuthService.verifyLogin(userAuth);
        });
        assertEquals("User account is inactive.", exception.getMessage());
        verify(userAuthRepository, times(1)).findByUserName(userAuth.getUsername());
    }

    @Test
    void TestAuthenticationFailed()
    {
        when(userAuthRepository.findByUserName(userAuth.getUsername()))
                .thenReturn(new UserAuth(2L, "mano", "$2a$12$5.bQ.EnTuA..QWU0UgPGAeBhRQCtu4/5bEFwHrM0cs4JFpzcEirzq", true, "user"));


        when(encoder.matches("m@123", "$2a$12$5.bQ.EnTuA..QWU0UgPGAeBhRQCtu4/5bEFwHrM0cs4JFpzcEirzq")).thenReturn(true);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        InvalidException exception = assertThrows(InvalidException.class, () -> {
           userAuthService.verifyLogin(userAuth);
        });
        assertEquals("internal error", exception.getMessage());
        verify(userAuthRepository, times(1)).findByUserName(userAuth.getUsername());
    }

    @Test
    void TestDeleteById() {
        when(this.userAuthRepository.findById(2L)).thenReturn(Optional.of(this.userAuth));
        String result = this.userAuthService.getDelete(this.userAuth.getUserId());
        assertEquals("Deleted", result);
        verify(this.userAuthRepository, times(1)).findById(2L);
    }

    @Test
    void TestRegisterUser() {

        when(this.userAuthRepository.save(this.userAuth)).thenReturn(this.userAuth);
        String result = this.userAuthService.registerUser(this.userAuth);
        assertEquals("Success", result);
        verify(this.userAuthRepository, times(1)).save(this.userAuth);
    }

}