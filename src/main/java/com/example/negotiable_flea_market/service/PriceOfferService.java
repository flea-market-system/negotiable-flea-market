package com.example.negotiable_flea_market.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.negotiable_flea_market.entity.Item;
import com.example.negotiable_flea_market.entity.PriceOffer;
import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.enums.OfferStatus;
import com.example.negotiable_flea_market.repository.ItemRepository;
import com.example.negotiable_flea_market.repository.PriceOfferRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceOfferService {

	private final PriceOfferRepository priceOfferRepository;
	private final ItemRepository itemRepository;
	private final LineBotService lineBotService;
	private final UserService userService;

	@Transactional
	public void createOffer(Long itemId, User buyer, Integer requestedPriceInt) {

		BigDecimal requestedPrice = new BigDecimal(requestedPriceInt);

		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("商品が見つかりません"));

		if (item.isSoldOut()) {
			throw new IllegalArgumentException("売り切れの商品には申請できません");
		}
		if (item.getSeller().getId().equals(buyer.getId())) {
			throw new IllegalArgumentException("自分の商品には申請できません");
		}
		if (priceOfferRepository.existsByItemAndBuyer(item, buyer)) {
			throw new IllegalArgumentException("この商品には既に申請済みです");
		}

		validatePrice(item.getPrice(), requestedPrice);

		PriceOffer offer = new PriceOffer();
		offer.setItem(item);
		offer.setBuyer(buyer);
		offer.setOriginalPrice(item.getPrice());
		offer.setRequestedPrice(requestedPrice);
		offer.setStatus(OfferStatus.REQUESTED);

		priceOfferRepository.save(offer);

		// --- 通知ロジック ---
		User seller = item.getSeller();

		// 1. 出品者本人への通知
		if (seller.getLineUserId() != null) {
			String sellerMsg = String.format(
					"【通知】出品中の商品「%s」に ¥%d の値下げ申請が届きました！",
					item.getName(),
					requestedPriceInt);
			lineBotService.sendMessage(seller.getLineUserId(), sellerMsg);
		}

		// 2. 管理者全員へのLINE通知
		List<User> admins = userService.getAdmins();
		String adminMsg = String.format("【管理者通知】商品「%s」に新しい値下げ申請がありました。", item.getName());

		for (User admin : admins) {
			if (admin.getLineUserId() != null && !admin.getId().equals(seller.getId())) {
				lineBotService.sendMessage(admin.getLineUserId(), adminMsg);
			}
		}
	}

	private void validatePrice(BigDecimal originalPrice, BigDecimal requestedPrice) {
		if (requestedPrice.compareTo(new BigDecimal("300")) < 0) {
			throw new IllegalArgumentException("申請価格は300円以上である必要があります");
		}

		if (requestedPrice.remainder(new BigDecimal("100")).compareTo(BigDecimal.ZERO) != 0) {
			throw new IllegalArgumentException("申請価格は100円単位で入力してください");
		}

		if (requestedPrice.compareTo(originalPrice) >= 0) {
			throw new IllegalArgumentException("現在の価格より安くする必要があります");
		}

		BigDecimal minPrice = originalPrice.multiply(new BigDecimal("0.85"));
		if (requestedPrice.compareTo(minPrice) < 0) {
			throw new IllegalArgumentException("値下げ申請は現在の価格の15%OFFまでです");
		}
	}

	@Transactional
	public void acceptOffer(Long offerId, User seller) {
		PriceOffer targetOffer = priceOfferRepository.findById(offerId)
				.orElseThrow(() -> new IllegalArgumentException("申請が見つかりません"));

		Item item = targetOffer.getItem();

		if (!targetOffer.getItem().getSeller().getId().equals(seller.getId())) {
			throw new SecurityException("権限がありません");
		}

		if (targetOffer.getStatus() != OfferStatus.REQUESTED) {
			throw new IllegalArgumentException("この申請は既に処理済みです");
		}

		// 1. 申請ステータスを承諾に変更
		targetOffer.setStatus(OfferStatus.ACCEPTED);
		targetOffer.setHoldsUntil(LocalDateTime.now().plusHours(24));
		priceOfferRepository.save(targetOffer);

		// ★ここを追加：商品の価格を申請価格で上書き保存
		item.setPrice(targetOffer.getRequestedPrice());
		itemRepository.save(item);

		// 2. 他の申請をすべて拒否にする
		List<PriceOffer> otherOffers = priceOfferRepository.findByItemAndStatus(targetOffer.getItem(),
				OfferStatus.REQUESTED);
		for (PriceOffer other : otherOffers) {
			if (!other.getId().equals(offerId)) {
				other.setStatus(OfferStatus.REJECTED);
				priceOfferRepository.save(other);
			}
		}

		// 3. 購入希望者（申請者）へのLINE通知
		User buyer = targetOffer.getBuyer();
		if (buyer.getLineUserId() != null) {
			String message = String.format(
					"【申請承認】「%s」への値下げ申請が承認されました！24時間以内に購入手続きをお願いします。",
					targetOffer.getItem().getName());
			lineBotService.sendMessage(buyer.getLineUserId(), message);
		}

		// 4. 管理者全員へのLINE通知
		List<User> admins = userService.getAdmins();
		// ※修正：元のコードは引数が足りずエラーになるため修正しました
		String adminMsg = String.format("【管理者通知】商品「%s」の値下げ申請が承認されました。", item.getName());

		for (User admin : admins) {
			if (admin.getLineUserId() != null && !admin.getId().equals(seller.getId())) {
				lineBotService.sendMessage(admin.getLineUserId(), adminMsg);
			}
		}
	}

	@Transactional
	public void rejectOffer(Long offerId, User seller) {
		PriceOffer targetOffer = priceOfferRepository.findById(offerId)
				.orElseThrow(() -> new IllegalArgumentException("申請が見つかりません"));

		if (!targetOffer.getItem().getSeller().getId().equals(seller.getId())) {
			throw new SecurityException("権限がありません");
		}

		if (targetOffer.getStatus() != OfferStatus.REQUESTED) {
			throw new IllegalStateException("この申請は既に処理済みです");
		}

		targetOffer.setStatus(OfferStatus.REJECTED);
		priceOfferRepository.save(targetOffer);
	}

	public List<PriceOffer> getOffersByItem(Item item) {
		return priceOfferRepository.findByItemOrderByCreatedAtDesc(item);
	}

	public List<PriceOffer> getWinningOffers(User buyer) {
		return priceOfferRepository.findByBuyerAndStatusOrderByUpdatedAtDesc(buyer, OfferStatus.ACCEPTED);
	}

	public List<PriceOffer> getOffersByBuyer(User buyer) {
		return priceOfferRepository.findByBuyerOrderByCreatedAtDesc(buyer);
	}

	public List<PriceOffer> getOffersBySeller(User seller) {
		return priceOfferRepository.findByItem_SellerOrderByCreatedAtDesc(seller);
	}
}