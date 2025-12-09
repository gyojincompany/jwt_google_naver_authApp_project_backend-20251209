package com.gyojincompany.home.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.gyojincompany.home.entity.AuthProvider;
import com.gyojincompany.home.entity.Role;
import com.gyojincompany.home.entity.User;
import com.gyojincompany.home.repository.UserRepository;


import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
//구글/네이버 로그인했을 때, 그 사용자 정보를 불러오고 → DB에 저장하거나 업데이트하는 역할을 하는 클래스 
//-> "OAuth2 로그인 사용자를 우리 서비스의 User로 바꿔주는 변환기"
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {	
        OAuth2User oAuth2User = super.loadUser(userRequest);
        //구글 또는 네이버에서 제공하는 사용자 정보(이름, 이메일 등)를 가져옴 -> 로그인 정보 제공자 정보 불러오기
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        //어떤 경로의 로그인인지 확인 (google or naver)
        
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        //구글인지 네이버인지에 따라 파싱 방식 다르게 설정하여 통일된 형태(OAuth2UserInfo) 로 변환함 -> 아래 메서드 정의되어 있음
        //-> 구글은 { email, name, sub(id) }, 네이버는 { response: { id, email, name } }
        
        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        } //이메일이 없으면 본인 확인이 안 되므로 오류 처리 
        
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase()); //공급처를 대문자로 변환
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, oAuth2UserInfo.getId());
        //DB에서 이 사용자가 있는지 확인 -> 구글이나 네이버에서 받은 providerId(고유ID) 로 조회
        
        User user;
        if (userOptional.isPresent()) { //이미 가입된 사용자일 경우 true
            user = userOptional.get(); //사용자 정보 가져오기
            user.setName(oAuth2UserInfo.getName()); //이미 가입된 사용자면 정보 업데이트
        } else { // 처음 로그인한 사용자면 새로 DB에 회원 정보 저장
            user = User.builder()
                    .email(oAuth2UserInfo.getEmail())
                    .name(oAuth2UserInfo.getName())
                    .password("") //비밀번호는 소셜로그인이므로 "" (빈 문자열)
                    .provider(provider)
                    .providerId(oAuth2UserInfo.getId())
                    .role(Role.USER)
                    .build();
        }
        
        userRepository.save(user);
        return user;
    }
    
    //구글 로그인인지? 네이버 로그인인지? 구분해서 각각 맞는 파싱 클래스(GoogleOAuth2UserInfo, NaverOAuth2UserInfo)를 만들어 주는 함수
    //구글과 네이버는 로그인 정보(json 구조)가 완전히 다르므로 이 메서드가 필요함
    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if ("naver".equals(registrationId)) {
            return new NaverOAuth2UserInfo(attributes);
        }
        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
    }
}
