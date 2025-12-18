package com.example.negotiable_flea_market.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.entity.UserComplaint;
import com.example.negotiable_flea_market.repository.UserComplaintRepository;
import com.example.negotiable_flea_market.repository.UserRepository;

@Service
public class AdminUserService {
	private final UserRepository userRepository;
	private final UserComplaintRepository complaintRepository;

	// コンストラクタインジェクション
	public AdminUserService(UserRepository userRepository,
			UserComplaintRepository complaintRepository) {
		this.userRepository = userRepository;
		this.complaintRepository = complaintRepository;
	}

	// 全ユーザー一覧を取得
	public List<User> listAllUsers() {
		return userRepository.findAll();
	}

	// 単一ユーザー取得
	public User findUser(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("User not found: " +
						id));
	}

	// 対象ユーザーの平均レビュー評価を取得(null の場合は 0 として返す)
	public Double averageRating(Long userId) {
		Double avg = userRepository.averageRatingForUser(userId);
		return (avg == null) ? 0.0 : avg;

	}

	// 指定ユーザーの通報件数を取得
	public long complaintCount(Long userId) {
		return complaintRepository.countByReportedUserId(userId);
	}

	// 指定ユーザーの通報履歴一覧を取得(新しい順) 
	public List<UserComplaint> complaints(Long userId) {
		return complaintRepository.findByReportedUserIdOrderByCreatedAtDesc(userId);

	}

	// ユーザーを BAN する処理(必要に応じてログインも無効化)
	@Transactional
	public void banUser(Long targetUserId, Long adminUserId, String reason, boolean alsoDisableLogin) {
		User u = findUser(targetUserId);
		u.setBanned(true);
		u.setBanReason(reason);
		u.setBannedAt(LocalDateTime.now());
		u.setBannedByAdminId(adminUserId == null ? null : adminUserId.intValue());
		if (alsoDisableLogin)
			u.setEnabled(false);
		userRepository.save(u);
	}

	// BAN解除
	@Transactional
	public void unbanUser(Long targetUserId) {
		User u = findUser(targetUserId);
		u.setBanned(false);
		u.setBanReason(null);
		u.setBannedAt(null);
		u.setBannedByAdminId(null);
		u.setEnabled(true); // BAN 解除後ログイン有効化 
		userRepository.save(u);
	}

}