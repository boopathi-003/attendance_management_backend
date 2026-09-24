package com.attendance.attendance_management.exceptionhandler;

import com.attendance.attendance_management.dto.ApiResponse;
import com.attendance.attendance_management.exceptionhandler.customexceptions.InputMandatoryException;
import com.attendance.attendance_management.exceptionhandler.customexceptions.InvalidException;
import com.attendance.attendance_management.exceptionhandler.customexceptions.UserAlreadyExistException;
import com.attendance.attendance_management.exceptionhandler.customexceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.InputMismatchException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotFoundException(UserNotFoundException ex) {
        return createApiResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiResponse<String>> handleUserAlreadyExistException(UserAlreadyExistException ex) {
        return createApiResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidException(InvalidException ex) {
        return createApiResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InputMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidException(InputMismatchException ex) {
        return createApiResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(InputMandatoryException.class)
    public ResponseEntity<ApiResponse<String>> handleInputMandatoryException(InputMandatoryException ex) {
        return createApiResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }


    private ResponseEntity<ApiResponse<String>> createApiResponse(HttpStatus status, String message) {
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(status.value());
        response.setStatus("error");
        response.setMessage(message);
        response.setValidationErrors(null);
        response.setRequestedTime(System.currentTimeMillis());
        response.setExecutionTime("0 ms");
        response.setResponse(null);
        return new ResponseEntity<>(response, status);
    }
}
