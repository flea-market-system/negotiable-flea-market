package com.example.negotiable_flea_market.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.negotiable_flea_market.entity.UserComplaint;

@Repository
public interface UserComplaintRepository extends JpaRepository<UserComplaint, Long> {
	long countByReportedUserId(Long reportedUserId);

	List<UserComplaint> findByReportedUserIdOrderByCreatedAtDesc(Long reportedUserId);
}
