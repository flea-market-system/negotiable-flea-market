// パッケージ宣言
package com.example.negotiable_flea_market.repository;
// エンティティのインポート
import com.example.negotiable_flea_market.FavoriteItem; 
import com.example.negotiable_flea_market.entity.Item;
import com.example.negotiable_flea_market.entity.User;
// Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository; // リポジトリアノテーション
import org.springframework.stereotype.Repository;


//コレクション/Optional 
import java.util.List; 
import java.util.Optional;

@Repository
public interface FavoriteItemRepository extends JpaRepository<FavoriteItem, Long> {
    // ユーザーと商品で一意に検索
    Optional<FavoriteItem> findByUserAndItem(User user, Item item);

    // ユーザーのお気に入り一覧取得
    List<FavoriteItem> findByUser(User user);

    // 既にお気に入り済みか存在チェック
    boolean existsByUserAndItem(User user, Item item);
}
