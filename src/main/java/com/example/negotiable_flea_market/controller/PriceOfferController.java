package com.example.negotiable_flea_market.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.service.PriceOfferService;
import com.example.negotiable_flea_market.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PriceOfferController {

    private final PriceOfferService priceOfferService;
    private final UserService userService;

    // 値下げ申請を受け付ける処理
    @PostMapping("/items/{itemId}/offers")
    public String requestPriceDrop(
            @PathVariable("itemId") Long itemId,
            @RequestParam("requestedPrice") Integer requestedPrice, // 画面からは整数で来る
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            // ログインユーザーを取得
            User buyer = userService.getUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Serviceを呼び出して申請実行
            priceOfferService.createOffer(itemId, buyer, requestedPrice);

            // 成功メッセージを入れて商品詳細へ戻る
            redirectAttributes.addFlashAttribute("successMessage", "値下げ申請を送信しました！");
            
        } catch (IllegalArgumentException e) {
            // ルール違反（15%超えなど）があったらエラーメッセージを出して戻る
            redirectAttributes.addFlashAttribute("errorMessage", "申請失敗: " + e.getMessage());
        }

        return "redirect:/items/" + itemId;
    }
}