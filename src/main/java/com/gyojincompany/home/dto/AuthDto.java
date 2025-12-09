package com.gyojincompany.home.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDto { //인증(auth) 기능에서 사용하는 DTO(데이터 전달 객체) 묶음
	//즉, 회원가입·로그인·토큰재발급 요청/응답에서 사용하는 데이터 형태를 정의한 클래스들을 한꺼번에 정의함
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignupRequest { //회원가입할 때 받는 데이터 저장용 DTO
        @NotBlank(message = "이메일은 필수 입력 항목입니다.") //@NotBlank → 빈 값 금지
        @Email(message = "잘못된 이메일 형식입니다.") //이메일 형식 검증
        private String email;
        
        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 6, message = "비밀번호는 반드시 6자 이상이어야 합니다.")
        private String password;
        
        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        private String name;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest { //회원 로그인 할때 받는 데이터 저장용 DTO
        @NotBlank(message = "이메일은 필수 입력 항목입니다.") //@NotBlank → 빈 값 금지
        @Email(message = "잘못된 이메일 형식입니다.") //이메일 형식 검증
        private String email;
        
        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        private String password;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse { //로그인 성공 시 보내주는 정보 저장용 DTO
        private String token;
        private String refreshToken;
        private String type = "Bearer";
        private String email;
        private String name;
        private String role;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest { //토큰 재발급 요청 DTO
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageResponse { //단순 메시지 응답 DTO
        private String message;
    }
}
