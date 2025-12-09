package com.gyojincompany.home.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
//프로젝트에서 오류가 발생하면 이 클래스가 빨아들여서, 깔끔한 JSON 형태로 사용자에게 에러 메시지를 보내주는 역할을 하는 에러 처리 클래스
public class GlobalExceptionHandler {	
    
    @ExceptionHandler(MethodArgumentNotValidException.class) 
    //입력값 검증 실패 처리 -> 회원가입 시 이메일이 비었을때, 비밀번호가 6글자 미만일때, name값이 null 일때 
    //{ "email": "잘못된 이메일 형식입니다.", "password": "비밀번호는 6자 이상이어야 합니다." } 이런 형태의 json으로 예쁜 형태로 반환
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    
    @ExceptionHandler(BadCredentialsException.class) //로그인 시 이메일 또는 비밀번호가 틀렸을 때
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "이메일 또는 비밀번호가 잘못 입력되었습니다.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(UsernameNotFoundException.class) //이메일로 조회 시 사용자를 못 찾았을 때 
    public ResponseEntity<Map<String, String>> handleUserNotFound(UsernameNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(RuntimeException.class) //일반적인 RuntimeException 오류 잡기(유저 없음, 권한 없음 오류, 삭제 불가 오류 등) -> 편의상 던지는 오류들
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class) //예상하지 못한 모든 오류 잡기(NullPointerException, DB 연결 오류 등)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "예상하지 못한 오류가 발생하였습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
