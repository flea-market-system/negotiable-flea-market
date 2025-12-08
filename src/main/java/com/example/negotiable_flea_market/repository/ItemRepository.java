package com.example.negotiable_flea_market.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.negotiable_flea_market.entity.Item;
import com.example.negotiable_flea_market.entity.User;

public interface ItemRepository extends JpaRepository<Item, Long> {
	// 名前の部分一致 + ステータスでページング検索（大文字小文字無視)
	Page<Item> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);

	// カテゴリ ID + ステータスでページング検索
	Page<Item> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

	// 名前の部分一致 + カテゴリ ID + ステータスでページング検索
	Page<Item> findByNameContainingIgnoreCaseAndCategoryIdAndStatus(String name, Long categoryId, String status,
			Pageable pageable);

	// ステータスのみでページング取得（公開中一覧など)
	Page<Item> findByStatus(String status, Pageable pageable);

	// 出品者ごとの商品一覧
	List<Item> findBySeller(User seller);
}
