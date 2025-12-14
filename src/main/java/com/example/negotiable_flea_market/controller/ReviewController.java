package com.example.negotiable_flea_market.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.negotiable_flea_market.entity.AppOrder;
import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.service.AppOrderService;
import com.example.negotiable_flea_market.service.ReviewService;
import com.example.negotiable_flea_market.service.UserService;

//このクラスが画面遷移用のコントローラであることを表すアノテーション 
@Controller
//このコントローラで扱う URL の先頭パスを /reviews に固定する 
@RequestMapping("/reviews")
public class ReviewController {
	// 評価(レビュー)に関するビジネスロジックを扱うサービス
	private final ReviewService reviewService;
	// 注文情報を取得するサービス(どの注文に対する評価かを判断するために使用) 
	private final AppOrderService appOrderService;
	// ユーザー情報を取得するサービス(評価者情報を取得するために使用)
	private final UserService userService;

	// コンストラクタインジェクションで必要なサービスを受け取る
	public ReviewController(ReviewService reviewService, AppOrderService appOrderService, UserService userService) {
			// 評価サービスをフィールドに設定 
			this.reviewService = reviewService;
			// 注文サービスをフィールドに設定 
			this.appOrderService = appOrderService;
			// ユーザーサービスをフィールドに設定 
			this.userService = userService;
		
			// 新規レビュー入力フォームを表示するためのハンドラ(GET /reviews/new/{orderId}) 
			@GetMapping("/new/{orderId}")
			public String showReviewForm(@PathVariable("orderId") Long orderId, Model model) {
				// orderId から対象の注文情報を取得し、存在しなければ例外を投げる 
				AppOrder order = appOrderService.getOrderById(orderId)
				.orElseThrow(() -> new IllegalArgumentException
				("Order not found."));
				
				// 画面で利用できるように注文情報を Model に格納
				model.addAttribute("order", order);
				// review_form.html(評価入力画面)のテンプレート名を返す 
				return "review_form";
			}
			// レビュー送信処理を行うハンドラ(POST /reviews) 
			@PostMapping
			public String submitReview(
					// ログイン中のユーザー情報(評価者)を Spring Security から取得 
					@AuthenticationPrincipal UserDetails userDetails,
					// 対象となる注文 ID をフォームから受け取る 
					@RequestParam("orderId") Long orderId,
					// 評価点(例:1〜5)をフォームから受け取る 
					@RequestParam("rating") int rating,
					// コメント(レビュー本文)をフォームから受け取る 
					@RequestParam("comment") String comment,
					// リダイレクト先へ一度だけ渡すメッセージ(フラッシュ属性) 
					// を設定するためのオブジェクト
					RedirectAttributes redirectAttributes) {
				// ログインユーザーのメールアドレスから User エンティティを取得し、
				// 存在しなければ例外を投げる
				User reviewer = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
				try {
					// 評価サービスを使ってレビューを登録する
					// (注文 ID・評価者・点数・コメントを渡す) 
					reviewService.submitReview(orderId, reviewer, rating, comment); 
					// 正常終了時のメッセージをフラッシュ属性に設定 
					redirectAttributes.addFlashAttribute("successMessage", "評価を送信しました!");
				} catch (IllegalStateException | IllegalArgumentException e) {
					// ビジネスロジック上の不正や状態異常が発生した場合、
					// エラーメッセージをフラッシュ属性に設定 
					redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
				}
				// 購入者側の注文履歴ページへリダイレクトする
				return "redirect:/my-page/orders"; // Redirect to buyer's order history
			}
	} //かっこが抜けてた
}