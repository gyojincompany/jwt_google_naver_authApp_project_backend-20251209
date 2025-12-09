package com.gyojincompany.home.service;

import lombok.RequiredArgsConstructor;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gyojincompany.home.dto.AuthDto.AuthResponse;
import com.gyojincompany.home.dto.AuthDto.LoginRequest;
import com.gyojincompany.home.dto.AuthDto.MessageResponse;
import com.gyojincompany.home.dto.AuthDto.RefreshTokenRequest;
import com.gyojincompany.home.dto.AuthDto.SignupRequest;
import com.gyojincompany.home.entity.AuthProvider;
import com.gyojincompany.home.entity.Role;
import com.gyojincompany.home.entity.User;
import com.gyojincompany.home.repository.UserRepository;
import com.gyojincompany.home.security.JwtUtil;

@Service
@RequiredArgsConstructor
//회원가입, 로그인, JWT 발급, 리프레시 토큰 갱신을 담당하는 서비스 클래스
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public MessageResponse signup(SignupRequest request) { //회원 가입 처리
        if (userRepository.existsByEmail(request.getEmail())) { //회원가입 요청이 들어옴 -> 이메일이 이미 존재하는지 확인
            throw new RuntimeException("이미 등록된 이메일 입니다.");
        }
        
        //이메일이 존재하지 않으면 회원가입 진행
        User user = User.builder() //비어있는 User 객체 만들기 -> 아래 내용 넣은 후 DB에 저장
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) //비밀번호는 반드시 암호화해서 저장
                .name(request.getName())
                .role(Role.USER) //권한은 기본 USER로 설정
                .provider(AuthProvider.LOCAL) //구글이나 네이버 같은 외부 로그인이 아닌 일반 회원 가입이므로 LOCAL로 저장
                .build();
        
        userRepository.save(user);
        
        return MessageResponse.builder()
                .message("회원 가입 성공!")
                .build();
    }
    
    public AuthResponse login(LoginRequest request) { //Spring Security 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken( //이메일 + 비밀번호가 맞는지 Spring Security가 알아서 검사함
                        request.getEmail(), 
                        request.getPassword()
                )
        );
        
        User user = (User) authentication.getPrincipal(); //인증된 User 객체 가져오기
        String token = jwtUtil.generateToken(user); //JWT 생성
        String refreshToken = jwtUtil.generateRefreshToken(user); //refresh token 생성 
        
        return AuthResponse.builder() //토큰 + 사용자 정보 반환
                .token(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
    
    public AuthResponse refreshToken(RefreshTokenRequest request) { //리프레시 토큰으로 새 액세스 토큰 발급
        String email = jwtUtil.extractUsername(request.getRefreshToken()); //리프레시 토큰에서 이메일 추출
        User user = userRepository.findByEmail(email) //DB에서 이메일로 user 조회
                .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));
        
        if (jwtUtil.validateToken(request.getRefreshToken(), user)) { //리프레시 토큰이 유효한지 확인
            String newToken = jwtUtil.generateToken(user); //새 access token 생성
            
            return AuthResponse.builder()
                    .token(newToken)
                    .refreshToken(request.getRefreshToken())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .build();
        } //리프레시 토큰은 그대로 두고, 액세스 토큰만 새로 발급해주는 구조임
        
        throw new RuntimeException("잘못된 리프레시 토큰입니다.");
    }
}
