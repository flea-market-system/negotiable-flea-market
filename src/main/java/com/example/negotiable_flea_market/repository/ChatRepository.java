// パッケージ宣言
package com.example.negotiable_flea_market.repository;


//エンティティのインポート
import com.example.negotiable_flea_market.entity.Chat;
import com.example.negotiable_flea_market.entity.Item;

//Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;
//リポジトリ・ステレオタイプ
import org.springframework.stereotype.Repository;
//取得結果のリスト型 
import java.util.List;


@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    // 指定商品のチャット履歴を作成日時昇順で取得
    List<Chat> findByItemOrderByCreatedAtAsc(Item item);
}
