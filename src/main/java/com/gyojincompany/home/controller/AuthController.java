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
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
