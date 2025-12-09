package com.gyojincompany.home.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
//JWT 토큰 생성 + JWT 토큰 내 정보 읽기 + 토큰이 유효한지 검사	이 3가지 기능을 담당하는 유틸리티 클래스
public class JwtUtil { //JwtUtil 클래스는 JWT를 만들고, 읽고, 검증하는 도구 상자라 생각하면 쉬움
    
	//application.properties 또는 application.yml 파일에 적혀 있는 값을 Java 변수에 자동으로 넣어주는 기능
	//예) jwt.secret=mysupersecretlongkey1234
    @Value("${jwt.secret}") //JWT를 만들 때 서명 시 사용하는 비밀 문자열
    private String secret;
    
    @Value("${jwt.expiration}") //Access Token(일반 JWT)의 유효기간(ms 단위)
    private Long expiration;
    
    @Value("${jwt.refresh-expiration}") //Refresh Token의 유효기간(ms) -> Access Token보다 훨씬 길게 설정됨.
    private Long refreshExpiration;
    
    private SecretKey getSigningKey() { //JWT 서명(Signature)용 키 가져오기 -> JWT에 서명할 도장 생성
    	//jwt.secret 값을 기반으로 서명에 사용할 key 생성 -> 이 키로 JWT를 “만듦” -> 이 키로 JWT가 “변조되지 않았는지 확인함”
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        //애플리케이션의 설정 파일 등에서 가져온 비밀 문자열(secret)을 암호화 알고리즘이 처리할 수 있는 바이트 배열로 변환합니다.
    }
    
    public String extractUsername(String token) { //토큰 안에서 email(=username) 추출
    	//JWT의 subject(username) 값 → 일반적으로 email 저장됨 -> 토큰에 담긴 사용자 이메일을 꺼내는 역할.
        return extractClaim(token, Claims::getSubject); //token내의 이메일 반환
        //함수형 인터페이스의 구현체로 전달되는 메서드 레퍼런스로 
        //JWT 표준에서 사용자 식별자를 저장하는 공간인 subject 클레임을 Claims 객체에서 추출하는 로직
    }
    
    public Date extractExpiration(String token) { //토큰 만료시간을 Date 타입으로 꺼내기
        return extractClaim(token, Claims::getExpiration); //token 만료시간 반환
        //Claims::getExpiration -> **함수형 인터페이스 (Function<Claims, Date>)**의 구현체로 전달되는 
        //**메서드 레퍼런스(Method Reference)**입니다. 이는 Claims 객체를 입력받아 그 객체의 getExpiration() 
        //메서드를 호출하여 만료 시간을 반환하겠다는 로직
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) { //Claims(토큰 내부 데이터) 꺼내기
    	//Claims = 토큰 안에 담긴 정보들(email, 만료시간, 커스텀 데이터 등) -> 다양한 정보를 공통 방식으로 꺼내기 위해 만든 유틸 함수
        final Claims claims = extractAllClaims(token); //주어진 JWT의 유효성을 검증하고 내부의 Claims를 추출
        return claimsResolver.apply(claims); //claims 객체 내의 만료 시간, 사용자 이름 등을 추출하여 반환
    }
    
    private Claims extractAllClaims(String token) { //JWT 전체 Claims 파싱(검증 포함)
        return Jwts.parser() //JWT 구문 분석 및 검증 작업을 시작하기 위한 JwtParser 빌더 객체 생성
                .verifyWith(getSigningKey()) //JWT를 secret key로 검증
                .build() //검증키를 바탕으로 실제로 분석할 JwtParser 객체 생성
                .parseSignedClaims(token) //실제로 토큰의 유효성 검사 및 토큰 파싱
                .getPayload(); //유효성이 검증된 토큰에서 Payload 부분 즉 Claims 객체를 최종적으로 추출하여 반환
    	//JWT를 secret key로 검증하고, 내부 Payload(Claims)를 꺼냄 -> “토큰이 변조되지 않았는지 먼저 검사하고 그 후에 토큰 안의 내용을 꺼내기”
    }
    
    private Boolean isTokenExpired(String token) { //토큰 만료시간이 지나면 true, 아니면 false를 반환
        return extractExpiration(token).before(new Date());
    }
    
    public String generateToken(UserDetails userDetails) { 
    	//Spring Security의 UserDetails 객체를 인자로 받아 새로운 Access Token을 String 형태로 반환
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), expiration);
    }
    
    public String generateRefreshToken(UserDetails userDetails) {
    	//Access Token이 만료되었을 때 새로운 Access Token을 발급받기 위해 사용되는 Refresh Token을 생성하여 String 형태로 반환
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
        //로그인할 때 액세스 토큰 + 리프레시 토큰을 동시에 발급하고, 액세스 토큰이 만료되었을 때 “이미 가지고 있던 리프레시 토큰”으로 새 토큰을 발급
    }
    
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) { //토큰 생성 함수
        return Jwts.builder()
                .claims(claims) //추가 데이터 넣기
                .subject(subject) //email(username) 넣기
                .issuedAt(new Date(System.currentTimeMillis())) //생성시간 넣기
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) //만료시간 넣기
                .signWith(getSigningKey()) //secret key로 만든 서명 넣기
                .compact(); //압축해서 문자열 형태의 JWT 만들기
        
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) { //토큰 검증
        final String username = extractUsername(token);
        //검증 기준: 토큰 안 email 과 DB의 email이 같고, 토큰이 만료되지 않았으면 → 유효한 토큰으로 간주
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    
    
    
}
