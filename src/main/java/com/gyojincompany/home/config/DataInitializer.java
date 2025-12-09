package com.gyojincompany.home.config;

import lombok.RequiredArgsConstructor;


import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.gyojincompany.home.entity.AuthProvider;
import com.gyojincompany.home.entity.Role;
import com.gyojincompany.home.entity.User;
import com.gyojincompany.home.repository.UserRepository;

//스프링부트가 시작될 때 자동으로 실행되며, 기본 관리자(admin) 계정과 일반 사용자(user) 계정을 미리 데이터베이스에 넣어주는 역할을 하는 클래스
//처음 서버를 실행했을 때 관리자 계정이 없어서 로그인 못 하는 상황을 막기 위해 생성하는 클래스
//-> 테스트용 기본 계정(user/admin)을 자동으로 준비해주는 용도
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
//CommandLineRunner를 구현했기 때문에 Spring Boot가 시작될 때 자동으로 run() 메서드가 실행됨
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // Admin 계정이 없으면 생성
        if (!userRepository.existsByEmail("admin@example.com")) { //DB에 "admin@example.com" 이메일 가진 계정이 없다면, 관리자 계정 DB에 생성
            User admin = User.builder()
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .name("Admin User")
                    .role(Role.ADMIN)
                    .provider(AuthProvider.LOCAL)
                    .build();
            userRepository.save(admin);
            System.out.println("Admin account created: admin@example.com / admin123");
        }
        
        // 일반 User 계정이 없으면 생성
        if (!userRepository.existsByEmail("user@example.com")) {
            User user = User.builder()
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .name("Regular User")
                    .role(Role.USER)
                    .provider(AuthProvider.LOCAL)
                    .build();
            userRepository.save(user);
            System.out.println("User account created: user@example.com / user123");
        }
    }
}
