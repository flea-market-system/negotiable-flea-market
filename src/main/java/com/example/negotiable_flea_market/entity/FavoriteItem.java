package com.example.negotiable_flea_market.entity;

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
@Data
@Table(name = "favorite_item")
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "users_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

}
