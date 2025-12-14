package com.example.negotiable_flea_market.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // このクラスは画面遷移(View の返却)を担当する Controller 層のクラスであることを示す 
public class LoginController {
	
	@GetMapping("/login") // ブラウザから /login への GET リクエストを受け取る 
	public String login() {
		return "login"; // login.html (templates/login.html) を返す。ログイン画面表示用。
	}
}