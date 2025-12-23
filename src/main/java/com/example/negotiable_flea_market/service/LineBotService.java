package com.example.negotiable_flea_market.service;
// ----------------------
// LINEの送信用サービスクラス
// ----------------------
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

// パッケージ（v8対応）
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.PushMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;

@Service
public class LineBotService {

	private final MessagingApiClient messagingApiClient;

    public LineBotService(MessagingApiClient messagingApiClient) {
        this.messagingApiClient = messagingApiClient;
    }

    public void sendMessage(String userId, String text) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        try {
            // v8では PushMessage ではなく "PushMessageRequest" を使います
            // メッセージはリスト形式（List.of）で渡す必要があります
            TextMessage textMessage = new TextMessage(text);
            
            PushMessageRequest request = new PushMessageRequest(
                userId,                 // 宛先
                List.of(textMessage),   // メッセージリスト
                false,                  // 通知オフにするか？(false=鳴らす)
                null                    // 集計用ユニットID(nullでOK)
            );

            // 送信実行（第一引数にUUID=リトライキーが必要です）
            messagingApiClient.pushMessage(UUID.randomUUID(), request).get();
            
            System.out.println("LINE送信成功: " + userId);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("LINE送信失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }
}