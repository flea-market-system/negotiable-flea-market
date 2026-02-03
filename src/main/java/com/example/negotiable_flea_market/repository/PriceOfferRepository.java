package com.example.negotiable_flea_market.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.negotiable_flea_market.entity.Item;
import com.example.negotiable_flea_market.entity.PriceOffer;
import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.enums.OfferStatus;

@Repository
public interface PriceOfferRepository extends JpaRepository<PriceOffer, Long> {

	// 「ある商品」に対して「あるユーザー」が既に申請しているかチェックする
	boolean existsByItemAndBuyer(Item item, User buyer);

	// 「ある商品」に来ている申請をすべて取得する（出品者が見る用）
	List<PriceOffer> findByItemOrderByCreatedAtDesc(Item item);

	// 自分の出した申請一覧を見る用
	List<PriceOffer> findByBuyerOrderByCreatedAtDesc(User buyer);

	List<PriceOffer> findByItemAndStatus(Item item, OfferStatus status);
	
	// 自分が「受信した」申請一覧（商品を出品している人＝自分）
    List<PriceOffer> findByItem_SellerOrderByCreatedAtDesc(User seller);

	// 承諾された（ACCEPTED）オファーを取り出す
	List<PriceOffer> findByBuyerAndStatusOrderByUpdatedAtDesc(User buyer, OfferStatus status);
	
	List<PriceOffer> findByStatusAndHoldsUntilBefore(OfferStatus status, LocalDateTime dateTime);
}