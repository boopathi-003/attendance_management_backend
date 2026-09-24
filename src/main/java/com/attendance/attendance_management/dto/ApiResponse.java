package com.attendance.attendance_management.dto;

import com.attendance.attendance_management.table.UserAuth;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String status;
    private String message;
    private Object validationErrors;
    private long requestedTime;
    private String executionTime;
    private T response;

    public ApiResponse(List<UserAuth> userList, String success) {
    }
}
