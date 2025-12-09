package com.gyojincompany.home.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.gyojincompany.home.entity.User;
import com.gyojincompany.home.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserRepository userRepository;
    
    @GetMapping("/dashboard") //관리자용 대시보드 정보 제공 요청
    public ResponseEntity<Map<String, Object>> adminDashboard(@AuthenticationPrincipal User admin) {
    	//JWT 인증 필터가 “이 요청 보낸 사람은 누구인지” 확인하기 위해 User 객체로 넣어주는 기능 -> 로그인한 관리자의 정보(admin 객체)를 자동으로 받음
        Map<String, Object> response = new HashMap<>(); //응답 json 파일 만들기
        response.put("message", "Welcome to Admin Dashboard"); //관리자 대시보드 환영 메시지 넣기
        response.put("admin", admin.getName()); //admin 계정의 username 넣기
        response.put("totalUsers", userRepository.count()); //모든 유저의 인원수를 DB에서 가져와서 넣기
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users") //전체 유저 목록 조회 요청
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {  
        List<User> users = userRepository.findAll(); //DB에서 모든 User 가져오기
        
        List<Map<String, Object>> userList = users.stream().map(user -> { //모든 user list 만들기
            Map<String, Object> userMap = new HashMap<>(); //한명의 user json 파일 만들기
            userMap.put("id", user.getId());
            userMap.put("email", user.getEmail());
            userMap.put("name", user.getName());
            userMap.put("role", user.getRole().name());
            userMap.put("provider", user.getProvider().name());
            userMap.put("createdAt", user.getCreatedAt());
            return userMap;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(userList);
    }
    
    @DeleteMapping("/users/{userId}") //특정 유저 삭제 요청
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) { //해당 userId가 존재하는지 DB에서 확인
            throw new RuntimeException("User not found");
        }
        
        userRepository.deleteById(userId); //DB에서 특정 userId 삭제
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully"); //삭제 성공시 성공 메시지 넣기
        
        return ResponseEntity.ok(response); //회원 삭제 성공 후 “User deleted successfully” 메시지 반환
    }
}
