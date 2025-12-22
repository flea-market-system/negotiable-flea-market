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

import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.service.ChatService;
import com.example.negotiable_flea_market.service.ItemService;
import com.example.negotiable_flea_market.service.UserService;

//このクラスが Web リクエストを処理するコントローラであることを示す 
@Controller
//このクラスで扱う URL パスの先頭を /chat に固定する 
@RequestMapping("/chat")
public class ChatController {
	// チャットメッセージに関するビジネスロジックを扱うサービス
	private final ChatService chatService;
	// 商品情報取得などを行うサービス
	private final ItemService itemService;
	// ユーザー情報取得などを行うサービス 
	private final UserService userService;

	// 必要なサービスをコンストラクタインジェクションで受け取る
	public ChatController(ChatService chatService, ItemService itemService, UserService userService) {
		// 引数で受け取った ChatService をフィールドに設定 
		this.chatService = chatService;
		// 引数で受け取った ItemService をフィールドに設定 
		this.itemService = itemService;
		// 引数で受け取った UserService をフィールドに設定
		this.userService = userService;
	}

	// 指定された商品に紐づくチャット画面を表示するハンドラ(GET /chat/{itemId}) 
	@GetMapping("/{itemId}")
	public String showChatScreen(@PathVariable("itemId") Long itemId, Model model) {
		// 商品 ID から商品情報を取得し、存在しなければ例外を投げる 
		model.addAttribute("item", itemService.getItemById(itemId)
				.orElseThrow(() -> new RuntimeException("Item not found")));
		// 対象商品のチャットメッセージ一覧を取得して Model に登録 
		model.addAttribute("chats", chatService.getChatMessagesByItem(itemId));
		// 商品詳細画面テンプレート(item_detail.html)を再利用してチャットを表示 
		return "item_detail"; // Re-use item_detail for chat display
	}

	// 指定された商品に対するチャットメッセージ送信を処理するハンドラ(POST /chat/{itemId}) 
	@PostMapping("/{itemId}")
	public String sendMessage(
		// パスから対象商品 ID を取得
		@PathVariable("itemId") Long itemId,
		// ログイン中のユーザー情報を Spring Security から取得 
		@AuthenticationPrincipal UserDetails userDetails,
		// フォームから送信されたメッセージ本文を取得 
		@RequestParam("message") String message) {
		// ログインユーザーのメールアドレスから User エンティティを取得(存在しなければ例外) 
		User sender = userService.getUserByEmail(userDetails.getUsername())
		.orElseThrow(() -> new RuntimeException("Sender not found")); 
		// サービスを通じてチャットメッセージを保存・送信処理 
		chatService.sendMessage(itemId, sender, message);
		//  ★修正点：商品詳細ページ（/items/...）へ戻るように変更
		//	チャット画面 (/chat/id)はisFavoritedを正しく持っていない。
		return "redirect:/items/" + itemId; 
	}
}