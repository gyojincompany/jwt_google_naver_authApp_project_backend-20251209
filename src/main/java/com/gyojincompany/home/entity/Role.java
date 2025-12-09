package com.gyojincompany.home.entity;

public enum Role { //사용자가 어떤 권한(등급)을 가지고 있는지 표시하는 값을 모아둔 enum
    USER, ADMIN
}

//이 코드는 “회원 등급 목록”이라고 보면 됨
//USER → 일반 사용자
//ADMIN → 관리자(모든 기능 접근 가능)
//-> 회원 한 명에게 위 둘 중 하나가 저장돼서 “이 사람은 일반 사용자냐? 관리자냐?” 를 판단하는 데 사용됨

//** 필요한 이유
//Spring Security에서 권한 체크할 때 사용
// /admin/** 주소는 ADMIN만 접근 가능
// 일반 페이지는 USER도 접근 가능
// -> 이런 식으로 접근 제어를 하기 위한 정보임