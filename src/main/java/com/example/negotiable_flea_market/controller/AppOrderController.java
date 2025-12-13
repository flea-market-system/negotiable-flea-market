package com.example.negotiable_flea_market.controller;

import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.service.AppOrderService;
import com.example.negotiable_flea_market.service.ItemService;
import com.example.negotiable_flea_market.service.UserService;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//注文・決済(Stripe 連携含む)に関する画面制御を行うコントローラークラス 
@Controller
//このコントローラが扱う URL の共通プレフィックスを /orders とする 
@RequestMapping("/orders")
public class AppOrderController {

	// アプリ全体の注文処理ロジックを担うサービス 
	private final AppOrderService appOrderService;
	// ユーザー情報取得などを行うサービス
	private final UserService userService;
	// 商品情報を扱うサービス(必要に応じて商品情報取得などで利用想定)
	private final ItemService itemService;

	// application.yml / properties から Stripe の公開鍵を読み込むフィールド 
	@Value("${stripe.public.key}")
	private String stripePublicKey;

	// コンストラクタインジェクションで必要なサービスを受け取る
	public AppOrderController(AppOrderService appOrderService, UserService userService, ItemService itemService) {
		// 注文サービスをフィールドに設定 
		this.appOrderService = appOrderService;
		// ユーザーサービスをフィールドに設定 
		this.userService = userService;
		// 商品サービスをフィールドに設定
		this.itemService = itemService;
	}

	// 購入処理開始用のエンドポイント(決済 Intent を作成し、クライアントシークレットを取得する) 
	@PostMapping("/initiate-purchase") // New endpoint to initiate purchase and get client secret 
	public String initiatePurchase(
			// 現在ログイン中のユーザー情報(UserDetails)を Spring Security から取得 
			@AuthenticationPrincipal UserDetails userDetails,
			// 購入対象商品の ID をリクエストパラメータから取得 
			@RequestParam("itemId") Long itemId,
			// リダイレクト先に一度だけ渡す属性を保持するためのオブジェクト
			RedirectAttributes redirectAttributes) {
		// ログインユーザーのメールアドレスから User エンティティを取得(見つからなければ例外) 
		User buyer = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Buyer not found"));
		try {
			// サービス層で Stripe の PaymentIntent を作成し、決済開始処理を行う
			PaymentIntent paymentIntent = appOrderService.initiatePurchase(itemId, buyer);
			// フロント側で Stripe Elements に渡すためのクライアントシークレットを Flash 属性に詰める 
			redirectAttributes.addFlashAttribute("clientSecret", paymentIntent.getClientSecret());
			// どの商品に対する決済かを保持するため、itemId も Flash 属性に詰める 
			redirectAttributes.addFlashAttribute("itemId", itemId);
			// 支払い確認画面へリダイレクト(Flash 属性が ModelAttribute として引き継がれる) 
			return "redirect:/orders/confirm-payment";
		} catch (IllegalStateException | IllegalArgumentException | StripeException e) {
			// ビジネスロジックの問題や Stripe 連携エラーが発生した場合はエラーメッセージを Flash 属性に
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			// エラーがあった場合は商品詳細画面にリダイレクトしてメッセージを表示させる 
			return "redirect:/items/" + itemId; // Redirect back to item detail with error
		}
	}

	// Stripe Elements を用いてクライアント側で決済確認を行う画面を表示する 
	@GetMapping("/confirm-payment") // Page to confirm payment with Stripe Elements 
	public String confirmPayment(
			// initiatePurchase からの FlashAttribute として渡された clientSecret を受け取る 
			@ModelAttribute("clientSecret") String clientSecret,
			// 同じく購入対象 itemId を ModelAttribute として受け取る 
			@ModelAttribute("itemId") Long itemId,
			// 画面に値を渡すための Model 
			Model model) {

		// 必要な情報がない場合は不正なアクセスとみなし、商品一覧へリダイレクト 
		if (clientSecret == null || itemId == null) {
			return "redirect:/items"; // Redirect if no payment intent data }
		}

		// テンプレートで Stripe 決済処理を行うために clientSecret を Model に格納 
		model.addAttribute("clientSecret", clientSecret);
		// 対象商品の ID も Model に格納
		model.addAttribute("itemId", itemId);
		// Stripe の公開鍵をフロント側に渡すため Model に格納
		model.addAttribute("stripePublicKey", stripePublicKey);
		// 決済確認用のビュー(payment_confirmation.html)を表示 
		return "payment_confirmation";
	}

	// クライアント側(Stripe.js)で決済完了後に呼び出されるエンドポイント
	@GetMapping("/complete-purchase") // Endpoint called by Stripe.js after payment is confirmed on client-side
	public String completePurchase(
			// クライアント側で取得した PaymentIntent の ID をクエリパラメータから受け取る 
			@RequestParam("paymentIntentId") String paymentIntentId,
			// 結果メッセージなどをリダイレクト先へ渡すためのオブジェクト 
			RedirectAttributes redirectAttributes) {
		try {
			// サービス層で PaymentIntent をもとに購入処理を確定させる(注文確定・在庫更新など) 
			appOrderService.completePurchase(paymentIntentId);
			// 正常に購入が完了した旨のメッセージを Flash 属性に設定 
			redirectAttributes.addFlashAttribute("successMessage", "商品を購入しました!");
			// 購入完了後にレビュー投稿ページへ遷移させるため、直近の完了注文 ID を取得
			// 実運用では PaymentIntent のメタデータなどから注文 ID を辿る設計が望ましい
			return appOrderService.getLatestCompletedOrderId()
					// 注文 ID が取得できた場合は、その ID を使って評価画面へリダイレクト
					.map(orderId -> "redirect:/reviews/new/" + orderId)
					// 取得に失敗した場合は、マイページの注文一覧へリダイレクトしエラーメッセージを表示 
					.orElseGet(() -> {
						redirectAttributes.addFlashAttribute("errorMessage",
								"購入は完了しましたが、評価ページへのリダイレクトに失敗しました。");
						return "redirect:/my-page/orders";
					});
		} catch (StripeException | IllegalStateException e) {
			// 決済処理やビジネスロジック中にエラーが発生した場合の処理 
			redirectAttributes.addFlashAttribute("errorMessage",
					"決済処理中にエラーが発生しました: " + e.getMessage());
			// 汎用的に商品一覧などへ戻す(別途エラー画面を用意してもよい)
			return "redirect:/items"; // Redirect to item list or a generic error page
		}
	}
	
	// Stripe Webhook の受信エンドポイント(概念のみ、本番運用では署名検証・イベント処理が必須) 
	@PostMapping("/stripe-webhook")
	public void handleStripeWebhook(

			// Stripe から送信される Webhook の生ペイロード(JSON 文字列) 
			@RequestBody String payload,
			// Stripe-Signature ヘッダ(署名検証に使用する値) 
			@RequestHeader("Stripe-Signature") String sigHeader) {
			// 実運用ではここで署名検証を行い、イベント種別ごとに処理を分岐させる
			// 例:payment_intent.succeeded / payment_intent.payment_failed など 
			System.out.println("Received Stripe Webhook: " + payload);
			// 例: if (event.getType().equals("payment_intent.succeeded")) { ... } といった処理を実装
	}
	
	// 販売者が自分の注文を「発送済み」に変更するためのエンドポイント 
	@PostMapping("/{id}/ship")
	public String shipOrder(
			// パスパラメータから注文 ID を取得
			@PathVariable("id") Long orderId,
			// 正常・異常メッセージをリダイレクト先に渡すためのオブジェクト 
			RedirectAttributes redirectAttributes) {
		try {
			// 指定された注文を発送済み状態に更新するようサービスへ依頼
			appOrderService.markOrderAsShipped(orderId);
			// 正常に更新できた場合のメッセージを Flash 属性に設定
			redirectAttributes.addFlashAttribute("successMessage", "商品を発送済みにしました。");
		} catch (IllegalArgumentException e) {
			// 注文が見つからない・状態的に不正などのケースで投げられる例外をキャッチ 
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		// 発送処理後は販売履歴画面(マイページの売上一覧)へリダイレクト 
		return "redirect:/my-page/sales";
	}
}

	
	
	
	
	
	
	
	
	
	
	

}
