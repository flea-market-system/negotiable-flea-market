package com.example.negotiable_flea_market.enums;

public enum OfferStatus {
    REQUESTED,  // 申請中
    ACCEPTED,   // 承諾済み（Hold中）
    REJECTED,   // 拒否された（手動または自動）
    EXPIRED,    // 期限切れ
    PURCHASED   // 購入完了
}