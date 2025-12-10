package com.example.negotiable_flea_market.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();

	}

	public Optional<User> getUserById(Long id) {
		return userRepository.findById(id);

	}

	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email);

	}

	@Transactional
	public User saveUser(User user) {
		// save に委譲
		return userRepository.save(user);
	}

	// 削除
	@Transactional
	public void deleteUser(Long id) {
		// ID 指定で削除
		userRepository.deleteById(id);
	}

	// 有効/無効フラグのトグル
	@Transactional
	public void toggleUserEnabled(Long userId) {
		// ID でユーザを取得（なければ 400 相当の例外）
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		// 既存の属性は変更せず enabled だけ反転
		user.setEnabled(!user.isEnabled());
		// 保存して確定
		userRepository.save(user);
	}

}
