package com.gyojincompany.home.entity;

public enum AuthProvider { //로그인한 사용자가 어떤 방식으로 인증했는지 표시하는 enum(열거형)
    LOCAL, GOOGLE, NAVER
}

//LOCAL → 우리 사이트 자체 회원가입으로 로그인한 사용자
//GOOGLE → 구글 로그인(OAuth2)으로 들어온 사용자
//NAVER → 네이버 로그인(OAuth2)으로 들어온 사용자

//즉, 회원 정보에 “이 사람은 어디로 로그인했지?” 하고 적어두기 위한 값들을 열거형으로 정의함

//** 필요한 이유
//구글 로그인한 사람은 비밀번호가 없을 수 있음
//네이버 로그인 사용자는 프로필 이메일 형식이 다름
//LOCAL 회원만 비밀번호 변경 가능
//-> 이런 차이를 구분하기 위해 인증 방식을 저장해두어야 함