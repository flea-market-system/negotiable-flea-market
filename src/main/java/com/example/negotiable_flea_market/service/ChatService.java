
// サービスのパッケージ
package com.example.negotiable_flea_market.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.negotiable_flea_market.entity.Chat;
import com.example.negotiable_flea_market.entity.Item;
import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.repository.ChatRepository;
import com.example.negotiable_flea_market.repository.ItemRepository;

@Service
public class ChatService {

	// チャットリポジトリの参照
	private final ChatRepository chatRepository;
	// 商品リポジトリの参照
	private final ItemRepository itemRepository;
	// LINE 通知サービスの参照
	private final LineNotifyService lineNotifyService;

	// 依存性をコンストラクタで注入
	public ChatService(
			ChatRepository chatRepository,
			ItemRepository itemRepository,
			LineNotifyService lineNotifyService) {
		this.chatRepository = chatRepository;
		this.itemRepository = itemRepository;
		this.lineNotifyService = lineNotifyService;
	}

	// 商品 ID に紐づくチャット履歴を昇順で取得
	public List<Chat> getChatMessagesByItem(Long itemId) {
		// 商品の存在を確認（なければ 400 相当の例外）
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("Item not found"));

		// 作成日時昇順でリストを返す
		return chatRepository.findByItemOrderByCreatedAtAsc(item);
	}

	// メッセージ送信：保存して相手に LINE 通知（可能なら）を行う
	public Chat sendMessage(Long itemId, User sender, String message) {
		// 対象商品を取得（存在しなければ例外）
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("Item not found"));

		// 新規チャットエンティティを構築
		Chat chat = new Chat();
		chat.setItem(item); // 商品を紐づけ
		chat.setSender(sender); // 送信者を紐づけ
		chat.setMessage(message); // 本文
		chat.setCreatedAt(LocalDateTime.now()); // 現在時刻

		// 保存して永続化
		Chat savedChat = chatRepository.save(chat);

		// 簡易実装：受信者を出品者とみなして通知
		User receiver = item.getSeller();

		// 受信者が通知トークンを設定していれば通知を送る
		if (receiver != null && receiver.getLineNotifyToken() != null) {
			String notificationMessage = String.format(
					"\n 商品「%s」に関する新しいメッセージが届きました！\n 送信者: %s\n メッセージ: %s",
					item.getName(),
					sender.getName(),
					message);
			// LINE Notify へ送信
			lineNotifyService.sendMessage(
					receiver.getLineNotifyToken(),
					notificationMessage);
		}

		// 保存結果を返却
		return savedChat;
	}
}
