package com.gyojincompany.home.security.oauth2;

import java.util.Map;

//네이버 로그인으로 받은 복잡한 JSON 구조에서 → id, name, email만 깔끔하게 꺼내주는 클래스
//여기서 중요한 정보(id, name, email)는 전부 "response" 안에 있음
//따라서,  getId(), getName(), getEmail()을 구현할 때, 반드시 response를 먼저 꺼내야 함
class NaverOAuth2UserInfo extends OAuth2UserInfo {
    public NaverOAuth2UserInfo(Map<String, Object> attributes) { //네이버에서 받아온 JSON 전체를 부모 클래스에 저장
        super(attributes);
    } 
    
    //네이버 JSON은 response 안에 실제 정보가 들어 있음 -> response를 꺼내서 그 안에서 다시 값을 찾음    
    @Override
    public String getId() { //네이버는 사용자 ID가 "response" → "id" 형태이므로 그걸 꺼내서 반환
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return (String) response.get("id");
    }
    
    @Override
    public String getName() { //이름도 "response" → "name"에서 꺼냄
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return (String) response.get("name");
    }
    
    @Override
    public String getEmail() { //이메일도 "response" → "email"에서 꺼냄
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return (String) response.get("email");
    }
}
