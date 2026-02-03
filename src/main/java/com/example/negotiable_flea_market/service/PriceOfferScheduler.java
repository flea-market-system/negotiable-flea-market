package com.example.negotiable_flea_market.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.negotiable_flea_market.entity.Item;
import com.example.negotiable_flea_market.entity.PriceOffer;
import com.example.negotiable_flea_market.enums.OfferStatus;
import com.example.negotiable_flea_market.repository.ItemRepository;
import com.example.negotiable_flea_market.repository.PriceOfferRepository;

@Component
public class PriceOfferScheduler {

    @Autowired
    private PriceOfferRepository priceOfferRepository;

    @Autowired
    private ItemRepository itemRepository;

    /**
     * 1分ごとに実行し、期限切れの交渉を失効させる
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void expireOldOffers() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 期限切れかつステータスが ACCEPTED（購入待ち）のものを取得
        List<PriceOffer> expiredOffers = priceOfferRepository
            .findByStatusAndHoldsUntilBefore(OfferStatus.ACCEPTED, now);

        for (PriceOffer offer : expiredOffers) {
            // ステータスを失効に変更
            offer.setStatus(OfferStatus.EXPIRED);
            
            // 商品のステータスを「商談中」から「出品中」に戻す
            Item item = offer.getItem();
            if ("商談中".equals(item.getStatus())) {
                item.setStatus("出品中");
                itemRepository.save(item);
            }
            
            priceOfferRepository.save(offer);
        }
        
        if (!expiredOffers.isEmpty()) {
            System.out.println(expiredOffers.size() + " 件の交渉期限切れを処理しました。");
        }
    }
}