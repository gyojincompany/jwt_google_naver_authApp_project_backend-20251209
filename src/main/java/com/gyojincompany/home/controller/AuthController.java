package com.gyojincompany.home.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gyojincompany.home.dto.AuthDto.AuthResponse;
import com.gyojincompany.home.dto.AuthDto.LoginRequest;
import com.gyojincompany.home.dto.AuthDto.MessageResponse;
import com.gyojincompany.home.dto.AuthDto.RefreshTokenRequest;
import com.gyojincompany.home.dto.AuthDto.SignupRequest;
import com.gyojincompany.home.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController { //로그인/회원가입/JWT 재발급을 담당하는 인증용 컨트롤러 -> 회원가입, 로그인, 리프레시 토큰으로 JWT 재발급을 처리하는 컨트롤러
    
    private final AuthService authService;
    
    @PostMapping("/signup") //AuthService가 이메일 중복 체크, 비밀번호 암호화, DB 저장 등을 처리한 후 -> "회원가입 성공!" 메시지 반환 
    public ResponseEntity<MessageResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }
    
    @PostMapping("/login") //AuthService가 이메일과 비번 검증 후 -> Access Token(JWT) 발급 -> Refresh Token 발급
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/refresh") //토큰 재발급 : AuthService는 refreshToken 유효한지 확인 -> 새 accessToken 발급 -> 필요하면 refreshToken도 갱신
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
