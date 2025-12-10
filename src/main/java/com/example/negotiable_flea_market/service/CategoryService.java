// src/main/java/com/example/fleamarketsystem/service/CategoryService.java
package com.example.negotiable_flea_market.service;

import com.example.negotiable_flea_market.entity.Category;
//サービスのパッケージ
package com.example.negotiable_flea_market.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

	// カテゴリリポジトリの参照
	private final CategoryRepository categoryRepository;

	// 依存性をコンストラクタで注入
	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	// すべてのカテゴリを取得
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	// 主キーでカテゴリを取得
	public Optional<Category> getCategoryById(Long id) {
		return categoryRepository.findById(id);
	}

	// 名称でカテゴリを取得（名称は一意前提）
	public Optional<Category> getCategoryByName(String name) {
		return categoryRepository.findByName(name);
	}

	// 新規/更新保存
	public Category saveCategory(Category category) {
		return categoryRepository.save(category);
	}

	// 削除
	public void deleteCategory(Long id) {
		categoryRepository.deleteById(id);
	}
}
