// リポジトリのパッケージ
package com.example.negotiable_flea_market.repository;

//エンティティと関連型の import
import com.example.negotiable_flea_market.entity.AppOrder;
import com.example.negotiable_flea_market.entity.User; 
//Spring Data JPA の import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


//コレクションや Optional 用 
import java.util.List; 
import java.util.Optional;


@Repository
public interface AppOrderRepository extends JpaRepository<AppOrder, Long> {
    // 買い手で注文一覧を取得
    List<AppOrder> findByBuyer(User buyer);

    // 出品者で注文一覧を取得（Item の seller 経由）
    List<AppOrder> findByItem_Seller(User seller);
}
