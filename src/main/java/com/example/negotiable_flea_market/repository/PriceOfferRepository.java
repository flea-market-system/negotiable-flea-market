package com.example.negotiable_flea_market.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.negotiable_flea_market.entity.Item;
import com.example.negotiable_flea_market.entity.PriceOffer;
import com.example.negotiable_flea_market.entity.User;

@Repository
public interface PriceOfferRepository extends JpaRepository<PriceOffer, Long> {

    // 「ある商品」に対して「あるユーザー」が既に申請しているかチェックする
    boolean existsByItemAndBuyer(Item item, User buyer);

    // 「ある商品」に来ている申請をすべて取得する（出品者が見る用）
    List<PriceOffer> findByItemOrderByCreatedAtDesc(Item item);
    
    // 自分の出した申請一覧を見る用
    List<PriceOffer> findByBuyerOrderByCreatedAtDesc(User buyer);
}