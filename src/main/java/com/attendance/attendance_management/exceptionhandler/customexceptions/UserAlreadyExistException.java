package com.attendance.attendance_management.exceptionhandler.customexceptions;

public class UserAlreadyExistException  extends RuntimeException{
    public UserAlreadyExistException(String message) {
        super(message);
    }
}
