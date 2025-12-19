package com.example.negotiable_flea_market.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

//　インポートを追記
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@Service
public class StripeService {
	// コンストラクタでシークレットキーを初期化
	public StripeService(@Value("${stripe.api.secretKey}") String secretKey) {
		// Stripe SDK に API キーを設定（スレッドセーフ）
		Stripe.apiKey = secretKey;
	}

	// 支払い意図(PaymentIntent)を作成
	public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String description)
			throws StripeException {
		// 通貨の最小単位へ変換（JPY なら 1 円→100 の係数不要だが Stripe は整数で受けるため×100は不要、しかし他通貨に備え共通化）
		long value = "jpy".equalsIgnoreCase(currency) ? amount.longValue()
				: amount.multiply(new BigDecimal(100)).longValue();
		// 作成パラメータをビルド
		PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
				// 金額（最小単位の整数）
				.setAmount(value)
				// 通貨コード
				.setCurrency(currency)
				// 説明
				.setDescription(description)
				// 自動支払い手段を有効化
				.setAutomaticPaymentMethods(
						PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
								.setEnabled(true)
								.build())
				.build();
		// PaymentIntent を作成して返す
		return PaymentIntent.create(params);
	}

	// 既存の PaymentIntent を取得
	public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
		// ID から取得
		return PaymentIntent.retrieve(paymentIntentId);
	}

}
