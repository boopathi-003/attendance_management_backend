package com.attendance.attendance_management.exceptionhandler.customexceptions;

public class InvalidException extends RuntimeException {
    public InvalidException(String message) {
        super(message);
    }
}
