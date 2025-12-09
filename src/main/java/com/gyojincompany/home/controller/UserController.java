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
public class UserController { //로그인한 사용자의 정보 조회(Profile)와 사용자 대시보드 정보를 제공하는 컨트롤러
    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@AuthenticationPrincipal User user) {
    	//@AuthenticationPrincipal User user -> 
    	//현재 로그인한 사람의 정보를 User 객체로 자동으로 받아옴 -> JWT 인증 필터가 넣어주는 것 -> 로그인한 유저를 DB에서 다시 꺼낼 필요 없음
        Map<String, Object> profile = new HashMap<>(); //현재 로그인한 유저의 정보를 보내주기 위한 json 만들기        
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("name", user.getName());
        profile.put("role", user.getRole().name());
        profile.put("provider", user.getProvider().name());
        profile.put("createdAt", user.getCreatedAt());
        
        return ResponseEntity.ok(profile); //현재 로그인한 유저의 정보 json 파일 반환
    }
    
    @GetMapping("/dashboard") //user 대시보드 보기 요청
    public ResponseEntity<Map<String, String>> userDashboard(@AuthenticationPrincipal User user) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to User Dashboard"); //유저 대시보드 환영 메시지 전달
        response.put("user", user.getName()); //현재 로그인한 username 전달
        response.put("role", user.getRole().name()); //현재 로그인한 유저의 role (USER) 전달 
        
        return ResponseEntity.ok(response);
    }
}
