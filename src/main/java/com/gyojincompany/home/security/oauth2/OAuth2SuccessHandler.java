package com.gyojincompany.home.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.gyojincompany.home.security.JwtUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
//OAuth2(구글 또는 네이버) 로그인 성공 후 JWT를 만들어서 리액트로 보내주는 역할을 하는 클래스
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	//구글 또는 네이버 로그인 성공 → JWT accessToken + refreshToken 생성 → 프론트엔드(React)로 redirect해서 전달
    
    private final JwtUtil jwtUtil;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        //OAuth2 로그인 후 Spring Security가 만들어놓은 인증 객체(authentication)에서 로그인한 사용자(UserDetails) 를 꺼냄
        String token = jwtUtil.generateToken(userDetails);
        //로그인 성공 → JWT(access token) 발급        
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        //refresh token도 함께 발급        
        
       //프론트엔드로 토큰 전달할 URL 만들기
//        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
		String targetUrl = UriComponentsBuilder.fromUriString("http://http://my-s3-hosting-server.s3-website.ap-northeast-2.amazonaws.com/oauth2/redirect") 
                .queryParam("token", token)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();        
        //http://localhost:3000/oauth2/redirect?token=xxx&refreshToken=yyy 이런 식의 URL을 만들어줌
                
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
      //-> 즉, 프론트 React가 받을 수 있게 쿼리 파라미터로 토큰 전달하는 것
    }
    
    /* 이 클래스가 하는 일 정리 :
		OAuth2 로그인 성공 감지 -> 	사용자 정보 가져오기 -> JWT access token & refresh token 생성 ->	React로 redirect하면서 토큰 보내줌
		그 후 React는 React에서 /oauth2/redirect 페이지를 만들고, URL에서 토큰을 꺼내서 localStorage나 쿠키에 저장! */
}
