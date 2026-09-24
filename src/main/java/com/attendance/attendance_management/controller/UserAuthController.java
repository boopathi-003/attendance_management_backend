package com.attendance.attendance_management.controller;

import com.attendance.attendance_management.dto.ApiResponse;
import com.attendance.attendance_management.exceptionhandler.customexceptions.InvalidException;
import com.attendance.attendance_management.services.UserAuthService;
import com.attendance.attendance_management.table.UserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserAuthController {
    private final UserAuthService authService;

    @GetMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserAuth>>> getRegisterUser() {
        return this.authService.getRegisterUser();
    }

    @GetMapping("/register/id/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<UserAuth>> getRegisterById(@PathVariable Long id) {
        long l;
        try {
            l = Long.parseLong(String.valueOf(id));
        } catch (NumberFormatException e) {
            throw new InvalidException("invalid type");
        }
        if (l < 1) {
            throw new InvalidException("input mismatch");
        }
        return this.authService.getRegisterById(l);
    }
    @PostMapping("/addregister")
    public String registerUser(@RequestBody UserAuth user) {
        return authService.registerUser(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody UserAuth userAuth) {
        return authService.verifyLogin(userAuth);
    }

    @DeleteMapping("/register/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String getDelete(@PathVariable String id) {
        return authService.getDelete(Long.parseLong(id));
    }
}
