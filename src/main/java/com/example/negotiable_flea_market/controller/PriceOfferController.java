package com.example.negotiable_flea_market.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.negotiable_flea_market.entity.Item;
import com.example.negotiable_flea_market.entity.PriceOffer;
import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.repository.ItemRepository;
import com.example.negotiable_flea_market.service.PriceOfferService;
import com.example.negotiable_flea_market.service.UserService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class PriceOfferController {

	private final PriceOfferService priceOfferService;
	private final UserService userService;
	private final ItemRepository itemRepository;

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

	// 出品者が申請一覧を見る画面
	@GetMapping("/items/{itemId}/offers")
	public String showOffersList(@PathVariable("itemId") Long itemId,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		Item item = itemRepository.findById(itemId).orElseThrow();
		User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow();

		// 出品者本人チェック
		if (!item.getSeller().getId().equals(currentUser.getId())) {
			return "redirect:/items/" + itemId;
		}

		// この商品に来ている申請一覧を取得
		model.addAttribute("item", item);
		model.addAttribute("offers", priceOfferService.getOffersByItem(item)); // ※1 下記参照

		return "offer_list"; // offer_list.htmlを表示
	}

	// 承諾ボタンが押された時の処理
	@PostMapping("/offers/{offerId}/accept")
	public String acceptOffer(@PathVariable("offerId") Long offerId,
			@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		try {
			User seller = userService.getUserByEmail(userDetails.getUsername()).orElseThrow();

			// Service実行
			priceOfferService.acceptOffer(offerId, seller);

			redirectAttributes.addFlashAttribute("successMessage", "申請を承諾しました！24時間キープされます。");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
		}

		// 元の一覧画面に戻る（itemIdを知るのが少し手間なので、一旦マイページか商品詳細に戻すのが楽ですが、
		// ここではHTTPヘッダーのRefererを使う手もあります。今回はシンプルにトップへ戻します）
		return "redirect:/";
	}
	
	// マイページ - 値下げ申請管理画面
    @GetMapping("/my-page/offers")
    public String showMyOffers(
            @RequestParam(name = "tab", defaultValue = "outgoing") String tab, // outgoing:送信済み, incoming:受信
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        User currentUser = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PriceOffer> offers;
        
        if ("incoming".equals(tab)) {
            // 自分宛ての申請（受信）
            offers = priceOfferService.getOffersBySeller(currentUser);
        } else {
            // 自分が送信した申請（送信）
            offers = priceOfferService.getOffersByBuyer(currentUser);
        }

        model.addAttribute("offers", offers);
        model.addAttribute("currentTab", tab); // ビューでタブのアクティブ判定に使用

        return "my_offers";
    }
}