package com.gyojincompany.home.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gyojincompany.home.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
//로그인할 때 이메일로 사용자를 찾아오는 역할만 하는 클래스
//-> Spring Security가 로그인 시 필요한 사용자 정보(UserDetails)를 DB에서 꺼내오는 기능을 구현한 클래스.
//Spring Security는 로그인할 때 이 동작을 실행 함 -> “사용자 이메일(username)로 회원 정보를 찾아와줘!”
//-> UserDetailsService 클래스의 loadUserByUsername() 을 자동 실행함 -> 이 메서드를 구현한 것임
	
//UserDetails는 스프링 시큐리티에서 "로그인한 사용자 정보"를 담기 위한 표준 인터페이스임
//DB에서 User 정보를 가져오면 User->UserDetails 형태로 변환해서 시큐리티에 넘겨짐
//개발자가 만든 User 클래스는 왜 바로 안 쓰이는가? -> 스프링 시큐리티는 User 클래스의 객체가 무슨 객체인지 알수 없음.
//그래서 직접 UserDetails를 구현해서 스프링이 이해하는 형태의 객체로 바꿔주어야 함.	
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email) //해당 email로 찾은 유저의 정보를 반환
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일에 대한 회원을 찾을 수 없습니다: " + email));
    }
}
