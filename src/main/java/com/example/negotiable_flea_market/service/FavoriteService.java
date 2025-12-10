// src/main/java/com/example/fleamarketsystem/service/FavoriteService.java
package com.example.negotiable_flea_market.service;

import com.example.negotiable_flea_market.entity.FavoriteItem;
import com.example.negotiable_flea_market.entity.Item;
import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.repository.FavoriteItemRepository;
import com.example.negotiable_flea_market.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

	// リポジトリの参照（お気に入り）
	private final FavoriteItemRepository favoriteItemRepository;
	// リポジトリの参照（商品）
	private final ItemRepository itemRepository;

	// 依存性をコンストラクタで注入
	public FavoriteService(
			FavoriteItemRepository favoriteItemRepository,
			ItemRepository itemRepository) {
		this.favoriteItemRepository = favoriteItemRepository;
		this.itemRepository = itemRepository;
	}

	// お気に入り追加（同一ユーザ×商品は一意）
	@Transactional
	public FavoriteItem addFavorite(User user, Long itemId) {
		// 商品存在チェック
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("Item not found"));

		// 既に登録済みならエラー（二重登録防止）
		if (favoriteItemRepository.existsByUserAndItem(user, item)) {
			throw new IllegalStateException("Item is already favorited by this user.");
		}

		// 新規のお気に入りエンティティ作成
		FavoriteItem favoriteItem = new FavoriteItem();
		favoriteItem.setUser(user); // ユーザ
		favoriteItem.setItem(item); // 商品

		// 保存して返す
		return favoriteItemRepository.save(favoriteItem);
	}

	// お気に入り解除
	@Transactional
	public void removeFavorite(User user, Long itemId) {
		// 商品取得（存在チェック）
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("Item not found"));

		// ユーザ×商品でお気に入りを取得（なければエラー）
		FavoriteItem favoriteItem = favoriteItemRepository
				.findByUserAndItem(user, item)
				.orElseThrow(() -> new IllegalStateException("Favorite not found."));

		// 削除実行
		favoriteItemRepository.delete(favoriteItem);
	}

	// お気に入りかどうかの判定
	public boolean isFavorited(User user, Long itemId) {
		// 商品取得（存在チェック）
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("Item not found"));

		// 存在するかどうかを返す
		return favoriteItemRepository.existsByUserAndItem(user, item);
	}

	// ユーザのお気に入り商品一覧を返す
	public List<Item> getFavoriteItemsByUser(User user) {
		// FavoriteItem から Item へマッピングして返す
		return favoriteItemRepository.findByUser(user).stream()
				.map(FavoriteItem::getItem)
				.collect(Collectors.toList());
	}
}
