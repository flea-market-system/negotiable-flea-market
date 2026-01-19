package com.example.negotiable_flea_market.service;

import java.math.BigDecimal; // ★追加
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

	@Transactional
	// ★引数の requestedPrice は Integer のままでOK（画面からは整数で来るため）
	public void createOffer(Long itemId, User buyer, Integer requestedPriceInt) {

		// 入力値をBigDecimalに変換して扱う
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

		// 3. 価格ルールのチェック（BigDecimal版）
		validatePrice(item.getPrice(), requestedPrice);

		// 4. 保存
		PriceOffer offer = new PriceOffer();
		offer.setItem(item);
		offer.setBuyer(buyer);
		offer.setOriginalPrice(item.getPrice()); // 型が合ったのでエラー消える
		offer.setRequestedPrice(requestedPrice); // 型が合ったのでエラー消える
		offer.setStatus(OfferStatus.REQUESTED);

		priceOfferRepository.save(offer);
	}

	// ★BigDecimal用のバリデーションロジック
	private void validatePrice(BigDecimal originalPrice, BigDecimal requestedPrice) {
		// 300円未満チェック
		// compareToは、A < B のとき「-1」を返します
		if (requestedPrice.compareTo(new BigDecimal("300")) < 0) {
			throw new IllegalArgumentException("申請価格は300円以上である必要があります");
		}

		// 100円単位チェック (remainderで割り算の余りを計算)
		// 0 じゃないならエラー
		if (requestedPrice.remainder(new BigDecimal("100")).compareTo(BigDecimal.ZERO) != 0) {
			throw new IllegalArgumentException("申請価格は100円単位で入力してください");
		}

		// 元値以上になっていないかチェック
		if (requestedPrice.compareTo(originalPrice) >= 0) {
			throw new IllegalArgumentException("現在の価格より安くする必要があります");
		}

		// 15%OFFチェック
		// originalPrice * 0.85
		BigDecimal minPrice = originalPrice.multiply(new BigDecimal("0.85"));

		// requestedPrice < minPrice ならエラー
		if (requestedPrice.compareTo(minPrice) < 0) {
			throw new IllegalArgumentException("値下げ申請は現在の価格の15%OFFまでです");
		}
	}

	@Transactional
	public void acceptOffer(Long offerId, User seller) {
		PriceOffer targetOffer = priceOfferRepository.findById(offerId)
				.orElseThrow(() -> new IllegalArgumentException("申請が見つかりません"));

		if (!targetOffer.getItem().getSeller().getId().equals(seller.getId())) {
			throw new SecurityException("権限がありません");
		}

		// 3. 既に別の申請が承諾されていないか確認
		// (Item自体が既にHOLD中かどうかのチェック。今回は簡易的にOfferの状態だけで判断します)
		if (targetOffer.getStatus() != OfferStatus.REQUESTED) {
			throw new IllegalArgumentException("この申請は既に処理済みです");
		}

		targetOffer.setStatus(OfferStatus.ACCEPTED);
		targetOffer.setHoldsUntil(LocalDateTime.now().plusHours(24)); // 24時間後まで確保
		priceOfferRepository.save(targetOffer);

		// 5. 副作用：同じ商品の「他の申請」をすべて「拒否」にする
		List<PriceOffer> otherOffers = priceOfferRepository.findByItemAndStatus(targetOffer.getItem(),
				OfferStatus.REQUESTED);
		for (PriceOffer other : otherOffers) {
			// 自分自身（今承諾したもの）以外をREJECTEDに
			if (!other.getId().equals(offerId)) {
				other.setStatus(OfferStatus.REJECTED);
				priceOfferRepository.save(other);
			}
		}

	}

}