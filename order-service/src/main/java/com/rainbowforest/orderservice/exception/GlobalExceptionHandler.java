package com.rainbowforest.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Hàm này chuyên đi bắt các lỗi khi Validation (@Valid) bên Controller bị kích hoạt
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        // Vòng lặp bóc tách: Sai ở ô input nào (Field) thì lấy đúng câu báo lỗi của ô đó (Message)
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        // Trả về một cục JSON kèm mã 400 Bad Request
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}