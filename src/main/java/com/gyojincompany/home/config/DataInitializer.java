package com.gyojincompany.home.config;

import lombok.RequiredArgsConstructor;


import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.gyojincompany.home.entity.AuthProvider;
import com.gyojincompany.home.entity.Role;
import com.gyojincompany.home.entity.User;
import com.gyojincompany.home.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // Admin 계정이 없으면 생성
        if (!userRepository.existsByEmail("admin@example.com")) {
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
