package com.clearsolutions.task.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class AppHandlerAdvice {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleException(MethodArgumentNotValidException e) {

        Map<String, String> collect = e.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : ""));

        return ResponseEntity.badRequest().body(collect);
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<Object> handleException(BusinessLogicException e) {
        Map<String, String> map = new HashMap<>();
        map.put("errorMessage", e.getMessage());
        return ResponseEntity.badRequest().body(map);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleException(DataIntegrityViolationException e) {
        Map<String, String> map = new HashMap<>();
        String message = "user with this email already exists";
        map.put("errorMessage", message);
        return ResponseEntity.badRequest().body(map);
    }

    @ExceptionHandler({DateTimeParseException.class})
    public ResponseEntity<Object> handleException(DateTimeParseException e) {
        Map<String, String> map = new HashMap<>();
        String message = "Date should be in the format YYYY-MM-DD";
        map.put("errorMessage", message);
        return ResponseEntity.badRequest().body(map);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleException(HttpMessageNotReadableException http) {
        Map<String, String> map = new HashMap<>();
        String message = "Bad HTTP request";
        map.put("errorMessage", message);
        return ResponseEntity.badRequest().body(map);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleException(MethodArgumentTypeMismatchException http) {
        Map<String, String> map = new HashMap<>();
        String message = "Bad url argument";
        map.put("errorMessage", message);
        return ResponseEntity.badRequest().body(map);
    }
}
