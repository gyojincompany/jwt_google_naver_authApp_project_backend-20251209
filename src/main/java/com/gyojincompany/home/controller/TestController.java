package com.gyojincompany.home.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	@GetMapping("/api/test")
	public String test() {
		return "test OK!!->무중단 배포 성공!!->수정2";
	}
			

}
