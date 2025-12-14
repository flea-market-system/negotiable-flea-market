package com.example.negotiable_flea_market.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.service.AppOrderService;
import com.example.negotiable_flea_market.service.FavoriteService;
import com.example.negotiable_flea_market.service.ItemService;
import com.example.negotiable_flea_market.service.ReviewService; // Add this import 
import com.example.negotiable_flea_market.service.UserService;

//画面遷移用の Controller クラスであることを示す 
@Controller
//URL の先頭パスを /my-page に設定 
@RequestMapping("/my-page")
public class UserController {
	// User 情報関連のビジネスロジックを扱うサービス 
	private final UserService userService;
	// 出品商品情報取得用サービス
	private final ItemService itemService;
	// 注文情報取得用サービス
	private final AppOrderService appOrderService;
	// お気に入り情報取得・追加・削除を扱うサービス
	private final FavoriteService favoriteService;
	// レビュー(評価)情報取得用サービス
	private final ReviewService reviewService; // Declare ReviewService

	// コンストラクタインジェクションにより依存サービスを受け取る
	public UserController(UserService userService, ItemService itemService,
			AppOrderService appOrderService,
			FavoriteService favoriteService, ReviewService reviewService) {
		// UserService の設定
		this.userService = userService;
		// ItemService の設定
		this.itemService = itemService;
		// AppOrderService の設定
		this.appOrderService = appOrderService;
		// FavoriteService の設定
		this.favoriteService = favoriteService;
		// ReviewService の設定
		this.reviewService = reviewService; // Initialize ReviewService
	}

	// マイページ表示(GET /my-page)
	@GetMapping
	public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログイン中ユーザーをメールアドレスから取得、存在しない場合は例外
		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// View で利用できるよう Model にログインユーザー情報を追加 
		model.addAttribute("user", currentUser);
		// my_page.html へ遷移
		return "my_page";
	}

	// 出品一覧(GET /my-page/selling)
	@GetMapping("/selling")
	public String mySellingItems(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログインユーザー取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// ログインユーザーが出品している商品一覧を Model に追加 
		model.addAttribute("sellingItems", itemService.getItemsBySeller(currentUser));
		// seller_items.html へ遷移
		return "seller_items";
	}

	// 購入履歴(GET /my-page/orders)
	@GetMapping("/orders")
	public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログインユーザー取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// ユーザーが購入した注文履歴を取得し Model へ追加 
		model.addAttribute("myOrders", appOrderService.getOrdersByBuyer(currentUser));
		// buyer_app_orders.html へ遷移
		return "buyer_app_orders";
	}

	// 販売履歴(GET /my-page/sales)
	@GetMapping("/sales")
	public String mySales(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログインユーザー取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// ユーザーが販売者として売った商品の注文一覧を Model へ追加 
		model.addAttribute("mySales", appOrderService.getOrdersBySeller(currentUser));
		// seller_app_orders.html へ遷移
		return "seller_app_orders";
	}

	// お気に入り一覧(GET /my-page/favorites)
	@GetMapping("/favorites")
	public String myFavorites(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログインユーザー取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// お気に入り商品一覧を Model に追加 
		model.addAttribute("favoriteItems", favoriteService.getFavoriteItemsByUser(currentUser));
		// my_favorites.html へ遷移
		return "my_favorites";
	}

	// 自分が投稿したレビュー一覧(GET /my-page/reviews)
	@GetMapping("/reviews")
	public String myReviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログインユーザー取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// 自分が投稿したレビュー一覧を Model へ格納
		model.addAttribute("reviews", reviewService.getReviewsByReviewer(currentUser));
		// user_reviews.html へ遷移
		return "user_reviews";
	}

}