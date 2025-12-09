package com.gyojincompany.home.security.oauth2;

import java.util.Map;

//구글 로그인으로 받은 사용자 정보를 → 우리가 쓸 수 있는 통일된 형태로 꺼내주는 클래스
//필요한 이유 -> 구글, 네이버, 카카오 등 각 로그인 제공자는 JSON 구조가 완전 다르므로 그대로 쓰면 코드가 뒤죽박죽됨
//-> 그래서 공통 부모 클래스 OAuth2UserInfo 를 만들고, 구글과 네이버 별로 정보를 꺼내는 방법만 다르게 구현하는 것임
class GoogleOAuth2UserInfo extends OAuth2UserInfo { //구글 로그인 데이터만 처리하는 클래스(부모는 OAuth2UserInfo)
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    } //생성자 -> 구글이 준 사용자 데이터 JSON을 그대로 부모한테 넘김
    
    @Override
    public String getId() { //구글에서 유저 고유 ID는 "sub"라는 키임 -> 그걸 꺼내서 반환
        return (String) attributes.get("sub");
    }
    
    @Override
    public String getName() { //구글 데이터에서 회원 이름을 "name" 키로 꺼냄
        return (String) attributes.get("name");
    }
    
    @Override
    public String getEmail() { //구글 이메일은 "email" 키로 꺼냄
        return (String) attributes.get("email");
    }
}
