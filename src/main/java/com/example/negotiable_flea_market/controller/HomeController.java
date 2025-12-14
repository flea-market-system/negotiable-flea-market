// ホーム画面(ルートパス "/")へのアクセス時の振る舞いを制御するコントローラ 
// src/main/java/com/example/fleamarketsystem/controller/HomeController.java 
package com.example.negotiable_flea_market.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//このクラスが Spring MVC のコントローラであることを示す
@Controller
public class HomeController {
	// アプリのルート URL("/")への GET アクセスを受け取るハンドラメソッド 
	@GetMapping("/")
	public String home(Authentication auth) {
		// 認証情報が null(未ログイン)または認証されていない場合は 
		// 商品一覧ページへリダイレクトする
		// 「未ログインでもとりあえず /items に飛ばす」というポリシー 
		if (auth == null || !auth.isAuthenticated()) {
			// 商品一覧画面(/items)へリダイレクト 
			return "redirect:/items";
		}
		// 認証済みの場合、ユーザーが ADMIN ロールを持っているかを判定する 
		boolean isAdmin = auth.getAuthorities().stream()
				.anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		// 管理者なら管理ユーザー一覧画面へ、それ以外は商品一覧画面へリダイレクト
		return isAdmin ? "redirect:/admin/users" : "redirect:/items";
	}
}