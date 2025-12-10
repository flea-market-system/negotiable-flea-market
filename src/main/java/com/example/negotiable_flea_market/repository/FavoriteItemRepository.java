// パッケージ宣言
package com.example.fleamarketsystem.repository;
// エンティティのインポート
import com.example.fleamarketsystem.entity.FavoriteItem; 
import com.example.fleamarketsystem.entity.Item;
import com.example.fleamarketsystem.entity.User;
// Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository; // リポジトリアノテーション
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteItemRepository extends JpaRepository<FavoriteItem, Long> {
    // ユーザーと商品で一意に検索
    Optional<FavoriteItem> findByUserAndItem(User user, Item item);

    // ユーザーのお気に入り一覧取得
    List<FavoriteItem> findByUser(User user);

    // 既にお気に入り済みか存在チェック
    boolean existsByUserAndItem(User user, Item item);
}
