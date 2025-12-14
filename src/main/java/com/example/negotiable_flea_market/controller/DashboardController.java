package com.example.negotiable_flea_market.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.repository.UserRepository;
import com.example.negotiable_flea_market.service.AppOrderService;
import com.example.negotiable_flea_market.service.ItemService;

//このクラスが Web リクエストを処理するコントローラであることを示すアノテーション 
@Controller
public class DashboardController {
	// ログインユーザー情報などを DB から取得するためのリポジトリ 
	private final UserRepository userRepository;
	// 商品情報(出品一覧など)を取得するためのサービス
	private final ItemService itemService;
	// 注文情報(売上・注文履歴など)を取得するためのサービス 
	private final AppOrderService appOrderService;

	// コンストラクタインジェクションで必要な依存オブジェクトを受け取る
	public DashboardController(UserRepository userRepository, ItemService itemService,
			AppOrderService appOrderService) {
		// 渡された UserRepository をフィールドに格納 
		this.userRepository = userRepository;
		// 渡された ItemService をフィールドに格納 
		this.itemService = itemService;
		// 渡された AppOrderService をフィールドに格納 
		this.appOrderService = appOrderService;
	}

	// ダッシュボード画面の表示を行うハンドラメソッド(GET /dashboard) 
	@GetMapping("/dashboard")
	public String dashboard(
			// 現在ログイン中のユーザー情報(UserDetails)を Spring Security から取得 
			@AuthenticationPrincipal UserDetails userDetails,
			// 画面に値を渡すための Model オブジェクト
			Model model) {

		// ログインユーザーのメールアドレスから User エンティティを検索し、
		// 存在しなければ例外を投げる
		User currentUser = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// ログインユーザーが管理者(ADMIN ロール)の場合の処理 
		if ("ADMIN".equals(currentUser.getRole())) {
			// 管理者用ダッシュボードに表示する最近の出品一覧を Model に追加 
			model.addAttribute("recentItems", itemService.getAllItems());
			// 管理者用ダッシュボードに表示する最近の注文一覧を Model に追加 
			model.addAttribute("recentOrders", appOrderService.getAllOrders()); // 管理者向けのダッシュボード画面テンプレートを返却
			return "admin_dashboard";
		} else {
			// 一般ユーザーの場合は管理ダッシュボードにはアクセスさせず、 
			// 商品一覧画面へリダイレクト
			return "redirect:/items";
		}
	}
}