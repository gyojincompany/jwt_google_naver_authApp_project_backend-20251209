package com.gyojincompany.home.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Entity
@Table(name = "users") //users 테이블과 연동
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder //반드시 사용해야 하는 것은 아니지만 아래의 장점이 있음
//** @Builder의 역할 -> 아래와 같이 코딩 가능함 -> 가독성 좋아지고 생성자 파라미터 순서 오류를 막아줌 (User.builder().name("홍길동").email("a@b.com"))
//User user = User.builder()
//.email("test@test.com")
//.password("1234")
//.name("홍길동")
//.role(Role.USER)
//.build();

public class User implements UserDetails, OAuth2User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; //회원 권한(USER, ADMIN) -> enum으로 정의
    
    @Enumerated(EnumType.STRING)
    private AuthProvider provider; //가입경로(LOCAL, GOOGLE, NAVER) -> enum으로 정의 
    
    private String providerId; //구글 또는 네이버에서 제공하는 고유 사용자 ID
    
    @Column(nullable = false, updatable = false) //수정 불가 설정
    private LocalDateTime createdAt; //회원 가입일
    
    @Column(nullable = false)
    private LocalDateTime updatedAt; //회원 정보 수정일
    
    @PrePersist //엔티티가 저장되기 직전에 현재 시간을 설정 → 데이터가 처음 DB에 저장될 때 자동 실행 	→ 가입 날짜/수정 날짜 자동 기록
    protected void onCreate() { //
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate //엔티티가 업데이트되기 직전에 현재 시간을 설정 → 회원 정보 업데이트 시 자동 실행
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override //유저 권한을 반환하는 메서드
    public Collection<? extends GrantedAuthority> getAuthorities() { //중요! 사용자의 권한을 Spring Security에 알려주는 부분
    	//role = USER → “ROLE_USER”,  role = ADMIN → “ROLE_ADMIN” 로 반환
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getUsername() { //유저 아이디로 email을 사용하겠다는 설정 -> Spring Security 내부 로그인 시 아이디로 email을 사용한다는 뜻.
        return email;
    }
    
    @Override //계정 유효기간 만료 여부
    public boolean isAccountNonExpired() { //계정 상태 (만료?) → 전부 true -> 계정 만료기능을 사용하지 않고 항상 true -> 정상 계정으로 처리함
        return true;
    }
    
    @Override //계정이 잠겼는지 여부
    public boolean isAccountNonLocked() { //계정 상태 (잠김?) → 전부 true -> 계정 잠금기능을 사용하지 않고 항상 true -> 정상 계정으로 처리함
        return true;
    }
    
    @Override //비밀번호 만료 여부
    public boolean isCredentialsNonExpired() { 
    	//비밀번호가 만료되었는지를 체크하는 기능 -> 만료되지 않으면 true, 만료되면 false
    	//-> 항상 true 이므로 비밀번호 만료기능을 사용하지 않겠다는 설정 -> 비밀번호는 만료없이 영원히 유효함
        return true;
    }
    
    @Override //계정 활성화 여부
    public boolean isEnabled() { //계정이 활성화 상태인지를 체크하는 기능
    	//이메일 인증 안되면 false, 관리자가 계정 비활성화면 false, 정지된 계정이면 false
        return true; //이 계정은 항상 활성화 상태 true로 로그인 가능하므로 정지/비활성화 기능을 구현하지 않겠다는 의미임 
    }
    
    // OAuth2User 인터페이스 구현
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap(); //지금은 OAuth2 로그인 시 넘겨받은 Google/Naver 정보(attributes)를 사용하지 않기 때문에 빈 값 반환
    }
    
    @Override
    public String getName() { //user의 이름 반환 메서드
        return name;
    }
}





