package com.attendance.attendance_management.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long userId;
    private String name;
    private String roll;
    private String department;
    private boolean isActive;
    private boolean isMarked;

    public UserDto(long l, String john, String teacher, String cse, String aTrue) {
    }
}
