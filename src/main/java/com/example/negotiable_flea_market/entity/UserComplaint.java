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
@Data
@Table(name = "user_complaint")
@AllArgsConstructor
@NoArgsConstructor
public class UserComplaint {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "reported_user_id", nullable = false)
	private Long reportedUserId;

	@Column(name = "reporter_user_id", nullable = false)
	private Long reporterUserId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String reason;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

}
