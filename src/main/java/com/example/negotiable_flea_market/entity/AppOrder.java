package com.example.negotiable_flea_market.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity

@Data
@Table(name = "app_order")
@AllArgsConstructor
@NoArgsConstructor
public class AppOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	@ManyToOne
	@JoinColumn(name = "buyer_id", nullable = false)
	private User buyer;

	@Column(nullable = false)
	private BigDecimal price;

	@Column(nullable = false)
	private String status = "購入済"; // 初期値

	// 追加: 作成日時(集計用)
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now(); // 変更: 初期値を消した。schemaの方にのみ記載

	// 追加: Stripe の PaymentIntent ID を保持(決済と注文を 1 対 1 で特定) 
	@Column(name = "payment_intent_id", unique = true)
	private String paymentIntentId;
	
	// 追加: 注文とレビューは 1対1
    // mappedBy = "order" は Reviewクラス側のフィールド名
    @OneToOne(mappedBy = "order")
    private Review review;
}
