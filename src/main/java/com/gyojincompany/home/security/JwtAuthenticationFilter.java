package com.gyojincompany.home.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
//클라이언트가 보내는 모든 요청에 있는 JWT 토큰을 검사해서, 해당 사용자를 Spring Security에 인증(로그인) 처리해주는 역할을 하는 클래스
public class JwtAuthenticationFilter extends OncePerRequestFilter { // //모든 요청마다 단 한번만 실행되는 필터->매 요청마다 JWT 검사하도록 함	
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization"); //요청에서 Authorization 헤더 읽기
        final String jwt;
        final String userEmail;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) { 
        	//Authorization 헤더가 없거나 Bearer로 시작하지 않으면 다음 필터로 넘김 -> JWT를 안 보낸 요청으로 판단하여 로그인 처리는 안하고 패스함
            filterChain.doFilter(request, response);
            return;
        }
        
        jwt = authHeader.substring(7); //"Bearer "는 7글자이므로 "Bearer "를 제외한 실제 JWT 문자열만 가져오기
        try {
            userEmail = jwtUtil.extractUsername(jwt);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) { 
            	//이미 로그인 상태인지 확인 -> 이미 로그인 인증된 상태이면 다시 인증하지 않음
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                //이 email을 가진 회원이 DB에 있는지 Spring Security가 인식할 수 있는 자료 타입인 UserDetails로 변환 후 조회
                
                if (jwtUtil.validateToken(jwt, userDetails)) { //JWT 유효성 검사 -> 서명 위조, 만료시간 경과, 이메일 일치 등 검사
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities() //해당 user에 대한 권한 설정
                        ); // 인증(로그인) 시키기 -> 이 사용자는 인증된 사용자라고 Spring Security에게 알려줌
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    //Spring Security에서 사용자를 수동으로 인증하고 인증 정보를 Security Context에 설정
                    
                    //**지금 들어온 요청은 로그인한 사용자의 요청이다라고 스프링에게 알려주는 코드로 하는 일 요약
        			//1) UsernamePasswordAuthenticationToken 객체 생성
        			//2) 비밀번호는 null (필요 없음)
        			//3) 해당 user에 대한 권한 설정
        			//4) SecurityContextHolder 에 저장
        			//→ 즉, 이 요청은 인증된 상태가 된다!
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        
        filterChain.doFilter(request, response); //JWT 검사 끝 -> 다음 필터 또는 컨트롤러로 계속 진행
    }
    
    /* JwtAuthenticationFilter 클래스의 전체 흐름 정리
    클라이언트가 API 요청할 때 "Authorization: Bearer (JWT)" 헤더를 보냄
    이 필터가 그 JWT를 꺼낸 후 토큰 안에 있는 email 꺼내서 토큰이 정상인지 확인하고
    정상이면 → “아 이사람 로그인된 사람!” 하고 인증 설정 후 인증 성공 상태를 SecurityContext에 저장함
    그 후 다음 필터 또는 컨트롤러로 요청 전달 */
}
