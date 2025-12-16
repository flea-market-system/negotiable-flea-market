package com.example.negotiable_flea_market.security;

import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository users; // User エンティティ操作用リポジトリ

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// usernameParameter("email") にしているためフォーム入力値は email 
		User u = users.findByEmailIgnoreCase(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		// enabled=false、banned=true の場合、ログインを拒否 
		if (!u.isEnabled())
			throw new DisabledException("Account disabled"); // アカウント無効化 
		if (u.isBanned())
			throw new DisabledException("Account banned"); // BAN 済ユーザー

		// Spring Security の UserDetails へ変換
		// 付与する権限は ROLE_ プレフィックスが必要
		return new org.springframework.security.core.userdetails.User(
				u.getEmail(), // 認証 ID(メール) 
				u.getPassword(), // ハッシュ化済パスワード 
				List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())) // 権限
		);
	}
}