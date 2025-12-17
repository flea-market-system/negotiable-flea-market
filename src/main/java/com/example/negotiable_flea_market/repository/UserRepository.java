package com.example.negotiable_flea_market.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.negotiable_flea_market.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByEmailIgnoreCase(String email);

	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.seller.id = :userId")
	Double averageRatingForUser(@Param("userId") Long userId);

}
