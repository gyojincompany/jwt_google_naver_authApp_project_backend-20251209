package com.gyojincompany.home.config;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import com.gyojincompany.home.security.JwtAuthenticationFilter;
import com.gyojincompany.home.security.oauth2.CustomOAuth2UserService;
import com.gyojincompany.home.security.oauth2.OAuth2SuccessHandler;

//결국 “백엔드 API에 누가 들어올 수 있나?”를 통제하는 문지기 역할을 하는 클래스가 SecurityConfig 임!
//->백엔드 API에 대한 “로그인 방식(JWT/OAuth2)” + “권한(ADMIN/USER)“ + “CORS” + “필터(JWT)” 설정을 모두 적어놓은 문지기 설정 파일

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) //SPA + JWT에서는 필요 없음 → 꺼버림
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/**","/api/test").permitAll() //어떤 URL을 로그인 과정 없이 열어줄지 설정                
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // api/admin 하위 요청은 관리자로 로그인했을때만 접근 가능하게 설정
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN") // api/user 하위 USER 또는 ADMIN 둘 다 접근 가능
                .anyRequest().authenticated() //나머지 요청은 모두 로그인 해야지만 접근가능하게 설정
            )
            .cors(cors -> {}) //중요! CORS 설정 활성화->하단의 CORS 설정을 사용한다는 설정
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //세션 사용 안함 설정 (JWT 방식이니까)
                //STATELESS->“서버가 로그인 정보를 세션에 저장하지 않는다” 는 뜻
                //즉, 서버가 사용자 정보를 기억하지 않음 → 매 요청마다 새로 인증해야 함 → 그래서 JWT 같은 토큰 방식이 필요함
                //세션을 사용하지 않는 이유:
                //전통적 방식 (SESSION)
                //서버가 로그인 정보를 메모리에 저장 -> 서버가 “이 사용자는 로그인된 상태”라고 기억하고 있음 -> 서버 1대일 때는 문제 없지만 서버가 여러 대면 복잡해짐
                //JWT 방식 (STATELESS)
                //서버는 로그인 상태를 전혀 저장하지 않음 -> 사용자(브라우저)가 매 요청마다 JWT를 보내고 -> 서버는 JWT만 보고 인증 여부를 판단
                //-> 모든 서버에서 같은 결과를 내기 때문에 확장에 유리함 -> 리액트 같은 프론트엔드와 스프링부트 백엔드를 분리하여 배포하는 경우 적용해야 함
            )
            .oauth2Login(oauth2 -> oauth2 //구글 로그인 설정
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2SuccessHandler)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); 
        	//“유저네임과 비밀번호 인증 필터보다 먼저 JWT 검사” -> 즉, 모든 요청에서 JWT부터 체크를 먼저한다는 설정
        
        return http.build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() { //일반 로그인(id와 비번 사용하는 로그인) 시 인증 실행
    	//AuthenticationProvider 타입의 객체를 생성하고 반환하는 메서드를 정의 -> 주석에 따라 일반 로그인 시 인증을 실행하는 역할
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(); 
        //**DaoAuthenticationProvider**의 인스턴스를 생성 -> Provider는 데이터 접근 객체(DAO)를 통해 사용자 정보를 인증하는 표준 구현체
        authProvider.setUserDetailsService(userDetailsService); //DB에서 사용자 찾기
        //DaoAuthenticationProvider가 사용자 정보를 DB 등에서 가져올 때 사용할 UserDetailsService 구현체를 설정
        authProvider.setPasswordEncoder(passwordEncoder()); //찾은 사용자의 비밀번호를 로그인할때 넣은 비밀번호와 암호화한 한 후 비교 
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    	//AuthenticationManager는 로그인 검사 기계의 역할임->스프링 시큐리티에서 아이디+비밀번호가 맞는지 검사하는 핵심 엔진
		//인증 성공(아이디와 비번 일치) 시, 인증된 Authentication 객체를 반환함 
		//Authentication 객체 내에 username, role(권한), 기타 UserDetails 정보가 들어 있음->컨트롤러에서 auth.getName() 하면 username 뺄 수 있음
        return config.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean 
	public CorsConfigurationSource corsConfigurationSource() {  
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration(); //CORS 설정 담길 객체 생성
		config.setAllowedOrigins(Arrays.asList("*")); // 모든 Origin 허용 -> 어디에서 오는 요청이든 다 허용
		config.setAllowedMethods(Arrays.asList("*")); // 모든 HTTP 메서드 허용 -> GET, POST, PUT, DELETE 등 전부 허용
		config.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더 허용 -> Authorization, Content-Type 같은 헤더 전부 허용	(JWT 받을 때 필요함!)
		config.setAllowCredentials(false); //쿠키 포함 여부 설정 -> “쿠키 기반 인증은 안 쓸 거야” (JWT 방식을 쓰니까 보통 false)
		config.applyPermitDefaultValues(); //기본 허용값 적용 -> CORS 기본 허용값도 같이 등록 (추가적인 기본 설정 포함)
		source.registerCorsConfiguration("/**", config); //URL 패턴에 적용 -> 모든 URL API 경로에 방금 만든 CORS 설정을 적용
		return source;
		//프론트 엔드, 모바일, 외부 서버 등 어디에서든 Spring API 요청할 수 있게 CORS 전부 열어둔 설정
	}
    
//    @Bean
//    public ForwardedHeaderFilter forwardedHeaderFilter() {
//        return new ForwardedHeaderFilter();
//    }
}
