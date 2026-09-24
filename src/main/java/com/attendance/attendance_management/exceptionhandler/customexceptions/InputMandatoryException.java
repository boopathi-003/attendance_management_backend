package com.attendance.attendance_management.exceptionhandler.customexceptions;

public class InputMandatoryException extends RuntimeException {
    public InputMandatoryException(String message) {
        super(message);
    }

}
