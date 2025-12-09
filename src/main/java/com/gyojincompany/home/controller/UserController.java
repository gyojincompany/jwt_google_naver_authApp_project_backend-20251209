package com.gyojincompany.home.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.gyojincompany.home.entity.User;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@AuthenticationPrincipal User user) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("name", user.getName());
        profile.put("role", user.getRole().name());
        profile.put("provider", user.getProvider().name());
        profile.put("createdAt", user.getCreatedAt());
        
        return ResponseEntity.ok(profile);
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> userDashboard(@AuthenticationPrincipal User user) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to User Dashboard");
        response.put("user", user.getName());
        response.put("role", user.getRole().name());
        
        return ResponseEntity.ok(response);
    }
}
