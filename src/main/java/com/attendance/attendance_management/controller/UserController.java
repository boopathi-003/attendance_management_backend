package com.attendance.attendance_management.controller;

import com.attendance.attendance_management.dto.UserDto;
import com.attendance.attendance_management.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @GetMapping("/roll/{roll}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<UserDto> getUsersByRoll(@PathVariable String roll) {
        return userService.getUsersByRoll(roll);
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<UserDto> getUsersByDepartment(@PathVariable String department) {
        return userService.getUsersByDepartment(department);
    }

    @PostMapping("/adduser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addUser(@RequestBody UserDto userDto) {
        String response = userService.addUser(userDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unmarked")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<UserDto> getUnmarkedUsers() {
        return userService.getUnmarkedUsers();
    }

    @DeleteMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> softDeleteUser(@PathVariable Long id) {
        String response = userService.softDeleteUser(id);
        return ResponseEntity.ok(response);
    }
}
