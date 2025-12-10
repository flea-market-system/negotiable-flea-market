// パッケージ宣言
package com.example.negotiable_flea_market.repository;
// コレクション/Optional 
import java.util.List; 
import java.util.Optional;
// Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository; 
// リポジトリアノテーション
import org.springframework.stereotype.Repository;
// エンティティのインポート
import com.example.negotiable_flea_market.entity.Review; 
import com.example.negotiable_flea_market.entity.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 出品者に紐づくレビュー一覧を取得
    List<Review> findBySeller(User seller);

    // 注文 ID に紐づくレビューを一件取得
    Optional<Review> findByOrderId(Long orderId);

    // レビューワ別のレビュー一覧を取得
    List<Review> findByReviewer(User reviewer);
}
