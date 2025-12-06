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
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Item {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "users_id", nullable = false)
	private User seller; //　usersテーブルへの外部キー。出品者だとわかりやすいようにsellerという名前。

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT") //長文想定でtext
	private String description;

	@Column(nullable = false)
	private BigDecimal price;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	private String status = "出品中"; //　デフォルトの値。値の変更はロジック側で行う。

	// 追加: 画像 URL(Cloudinary にアップロードした結果を格納) 
	// For image URLs (Cloudinary)
	private String imageUrl;

	// 追加: 作成日時。列名を created_at に固定、初期値は現在時刻 
	@Column(name = "created_at", nullable = false) // New field 
	private LocalDateTime createdAt = LocalDateTime.now();

}
