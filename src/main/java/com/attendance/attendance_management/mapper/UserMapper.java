package com.attendance.attendance_management.mapper;

import com.attendance.attendance_management.dto.UserDto;
import com.attendance.attendance_management.table.UserInfo;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(UserInfo userInfo) {
        return new UserDto(
                userInfo.getUserId(),
                userInfo.getName(),
                userInfo.getRoll(),
                userInfo.getDepartment(),
                userInfo.isActive(),
                userInfo.isMarked()
        );
    }

    public UserInfo toEntity(UserDto userDto) {
        UserInfo userInfo = new UserInfo();
        userInfo.setName(userDto.getName());
        userInfo.setDepartment(userDto.getDepartment());
        userInfo.setRoll(userDto.getRoll());
        return userInfo;
    }

}

