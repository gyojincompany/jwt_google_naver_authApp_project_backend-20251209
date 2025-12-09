package com.gyojincompany.home.security.oauth2;

import java.util.Map;

//구글 또는 네이버에서 가져온 사용자 정보를 “같은 형식”으로 변환하기 위한 부모 클래스(템플릿)
//-> 구글, 네이버는 로그인 정보 JSON 구조가 서로 다르기 때문에 provider(google/naver)마다 구조에 맞게 데이터를 꺼내는 방법이 다름
//그런데 서비스 코드에서 매번 구글/네이버 다르게 처리하면 코드가 너무 복잡해짐 -> 그래서 만든 것이 OAuth2UserInfo (부모 클래스)
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;
    //attributes : Google 또는 Naver에서 받아온 JSON 전체 데이터를 Map으로 저장 ->  protected라서 자식 클래스에서 접근 가능
    
    public OAuth2UserInfo(Map<String, Object> attributes) { //구글/네이버에서 받은 데이터를 저장하는 생성자
        this.attributes = attributes;
    }    
    
    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    // 각 provider에서 ID, 이름, 이메일을 어떻게 꺼낼지는 "자식 클래스가 반드시 구현하도록" 강제함 -> 그래서 abstract(추상 메서드)로 선언!
    
    /* 구글 구현
		@Override
		public String getId() {
		    return (String) attributes.get("sub");
		}
		
		네이버 구현
		@Override
		public String getId() {
		    return ((Map)attributes.get("response")).get("id");
		}
     */
}


