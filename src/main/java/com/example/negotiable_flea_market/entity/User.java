package com.example.negotiable_flea_market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true) // 追加: schemaのemail VARCHAR(255) NOT NULL UNIQUEに合わせた。
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String role;

	// LINE Notify のアクセストークン（任意）
	@Column(name = "line_notify_token")
	private String lineNotifyToken;

	// 追加: アカウントの有効/無効フラグ。初期値は true(有効) @Column(nullable = false)
	private boolean enabled = true; // New field
}
