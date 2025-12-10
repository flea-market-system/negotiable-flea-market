package com.example.negotiable_flea_market.service;

import java.util.OptionalDouble;

import org.springframework.stereotype.Service;

import com.example.negotiable_flea_market.entity.AppOrder;
import com.example.negotiable_flea_market.entity.Review;
import com.example.negotiable_flea_market.repository.AppOrderRepository;
import com.example.negotiable_flea_market.repository.ReviewRepository;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;
	// 注文リポジトリの参照
	private final AppOrderRepository appOrderRepository;

	// 依存性をコンストラクタ注入
	public ReviewService(ReviewRepository reviewRepository, AppOrderRepository appOrderRepository) {
		// レビューリポジトリを設定
		this.reviewRepository = reviewRepository;
		// 注文リポジトリを設定
		this.appOrderRepository = appOrderRepository;
	}

	// レビュー投稿（買い手のみ、1 注文 1 レビュー）
	@Transactional
	public Review submitReview(Long orderId, User reviewer, int rating, String comment) {
		// 注文を取得（存在しなければ 400 相当）
		AppOrder order = appOrderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found."));
		// 注文の買い手と同一ユーザか検証
		if (!order.getBuyer().getId().equals(reviewer.getId())) {
			// 買い手以外は拒否
			throw new IllegalStateException("Only the buyer can review this order.");
		}
		// 既にレビュー済みかを検査
		if (reviewRepository.findByOrderId(orderId).isPresent()) {
			// 二重レビューを防ぐ
			throw new IllegalStateException("This order has already been reviewed.");
		}
		// 新しいレビューエンティティを構築
		Review review = new Review();
		// 注文を紐付け
		review.setOrder(order);
		// レビュワーを設定
		review.setReviewer(reviewer);
		// 出品者（注文の商品の出品者）を設定
		review.setSeller(order.getItem().getSeller());
		// 対象商品を設定
		review.setItem(order.getItem());
		// 評価点を設定
		review.setRating(rating);
		// コメントを設定
		review.setComment(comment);
		// 保存して返却
		return reviewRepository.save(review);
	}

	// 出品者に対するレビュー一覧を取得
	public List<Review> getReviewsBySeller(User seller) {
		// リポジトリに委譲
		return reviewRepository.findBySeller(seller);
	}

	// 出品者に対する平均評価を算出
	public OptionalDouble getAverageRatingForSeller(User seller) {
		// ストリームで平均を計算
		return reviewRepository.findBySeller(seller).stream()
				.mapToInt(Review::getRating)
				.average();
	}

	// あるレビュワーが書いたレビューを取得
	public List<Review> getReviewsByReviewer(User reviewer) {
		// リポジトリに委譲
		return reviewRepository.findByReviewer(reviewer);
	}

}
