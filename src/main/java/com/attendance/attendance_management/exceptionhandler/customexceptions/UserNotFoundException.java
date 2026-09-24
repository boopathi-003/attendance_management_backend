package com.attendance.attendance_management.exceptionhandler.customexceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
