package com.example.negotiable_flea_market.entity;

import java.time.LocalDateTime;

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

	// 修正後（カラム名を変えます）
	@Column(name = "line_user_id")
	private String lineUserId;

	// 追加: アカウントの有効/無効フラグ。初期値は true(有効) 
	@Column(nullable = false)
	private boolean enabled = true; // New field

	@Column(nullable = false)
	private boolean banned = false; //banフラグ

	@Column(name = "ban_reason")
	private String banReason;

	@Column(name = "banned_at")
	private LocalDateTime bannedAt;

	@Column(name = "banned_by_admin_id")
	private Integer bannedByAdminId;

}
