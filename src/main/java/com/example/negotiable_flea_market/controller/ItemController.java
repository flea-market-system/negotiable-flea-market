package com.example.negotiable_flea_market.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.negotiable_flea_market.entity.Category;
import com.example.negotiable_flea_market.entity.Item;
import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.service.CategoryService;
import com.example.negotiable_flea_market.service.ChatService;
import com.example.negotiable_flea_market.service.FavoriteService;
import com.example.negotiable_flea_market.service.ItemService;
import com.example.negotiable_flea_market.service.ReviewService; // Add this import 
import com.example.negotiable_flea_market.service.UserService;

//商品一覧・詳細・登録・編集・削除・お気に入りなど、商品に関する画面制御を行うコントローラ 
@Controller
//このコントローラで扱う URL のプレフィックスを /items に固定
@RequestMapping("/items")
public class ItemController {

	// 商品情報の検索・取得・保存を行うサービス 
	private final ItemService itemService;
	// カテゴリ情報を扱うサービス
	private final CategoryService categoryService;
	// ユーザー情報を扱うサービス
	private final UserService userService;
	// 商品ごとのチャットメッセージを扱うサービス 
	private final ChatService chatService;
	// お気に入り機能を扱うサービス
	private final FavoriteService favoriteService;
	// レビュー(評価)情報を扱うサービス
	private final ReviewService reviewService; // Declare ReviewService

	// 必要なサービスをコンストラクタインジェクションで受け取る 
	public ItemController(
			ItemService itemService,
			CategoryService categoryService,
			UserService userService,
			ChatService chatService,
			FavoriteService favoriteService,
			ReviewService reviewService) {
		// 商品サービスをフィールドへ設定
		this.itemService = itemService;
		// カテゴリサービスをフィールドへ設定
		this.categoryService = categoryService;
		// ユーザーサービスをフィールドへ設定
		this.userService = userService;
		// チャットサービスをフィールドへ設定
		this.chatService = chatService;
		// お気に入りサービスをフィールドへ設定
		this.favoriteService = favoriteService;
		// レビューサービスをフィールドへ設定
		this.reviewService = reviewService; // Initialize ReviewService

	}

	// 商品一覧画面の表示(検索・カテゴリ絞り込み・ページングに対応) @GetMapping
	public String listItems(
			// キーワード検索用パラメータ(任意)
			@RequestParam(value = "keyword", required = false) String keyword,
			// カテゴリ ID による絞り込み用パラメータ(任意)
			@RequestParam(value = "categoryId", required = false) Long categoryId,
			// ページ番号(0 始まり)、指定が無ければ 0 ページ目 
			@RequestParam(value = "page", defaultValue = "0") int page,
			// 1 ページあたりの件数、指定が無ければ 10 件
			@RequestParam(value = "size", defaultValue = "10") int size,
			// 画面に値を渡すための Model
			Model model) {
		// 検索条件・ページ情報に基づき商品一覧(Page)を取得
		Page<Item> items = itemService.searchItems(keyword, categoryId, page, size); // カテゴリ一覧を取得(検索フォームのプルダウンなどに利用)
		List<Category> categories = categoryService.getAllCategories();

		// 取得した商品情報を Model へ渡す 
		model.addAttribute("items", items);
		// カテゴリ一覧を Model へ渡す 
		model.addAttribute("categories", categories);
		// 商品一覧画面のテンプレート名を返却 
		return "item_list";
	}

	// 商品詳細画面の表示(チャット・お気に入り・出品者評価などを含む) 
	@GetMapping("/{id}")
	public String showItemDetail(
			// パスパラメータから商品 ID を取得
			@PathVariable("id") Long id,
			// ログインユーザー情報(未ログインの場合は null になり得る) 
			@AuthenticationPrincipal UserDetails userDetails,
			// 画面に値を渡すための Model
			Model model) {
		// 商品 ID から商品を取得。存在しない場合は一覧へリダイレクト 
		Optional<Item> item = itemService.getItemById(id);
		if (item.isEmpty()) {
			// 対象商品が存在しない場合は商品一覧へ戻す 
			return "redirect:/items"; // Item not found
		}
		// 取得した商品を Model に格納
		model.addAttribute("item", item.get());
		// 対象商品のチャットメッセージ一覧を Model に格納
		model.addAttribute("chats", chatService.getChatMessagesByItem(id));

		// 出品者の平均評価を取得して、存在する場合のみ Model へ設定 
		reviewService.getAverageRatingForSeller(item.get().getSeller())
				// 小数 1 桁でフォーマットして"sellerAverageRating"として渡す 
				.ifPresent(avg -> model.addAttribute("sellerAverageRating",
						String.format("%.1f", avg)));
		// ログインユーザーがいる場合のみ、お気に入りフラグを判定 
		if (userDetails != null) {
			// ログインユーザーの User エンティティを取得
			User currentUser = userService.getUserByEmail(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));
			// 現在のユーザーがこの商品をお気に入り登録済みかどうかを判定し Model に渡す 
			model.addAttribute("isFavorited", favoriteService.isFavorited(currentUser, id));
		}
		//商品詳細画面テンプレートを返却
		return "item_detail";
	}

	// 商品詳細画面の表示(チャット・お気に入り・出品者評価などを含む) 
	@GetMapping("/{id}")
	public String showItemDetail(
			// パスパラメータから商品 ID を取得
			@PathVariable("id") Long id,
			// ログインユーザー情報(未ログインの場合は null になり得る) 
			@AuthenticationPrincipal UserDetails userDetails,
			// 画面に値を渡すための Model
			Model model) {
		// 商品 ID から商品を取得。存在しない場合は一覧へリダイレクト 
		Optional<Item> item = itemService.getItemById(id);
		if (item.isEmpty()) {
			// 対象商品が存在しない場合は商品一覧へ戻す 
			return "redirect:/items"; // Item not found
		}
		// 取得した商品を Model に格納
		model.addAttribute("item", item.get());
		// 対象商品のチャットメッセージ一覧を Model に格納
		model.addAttribute("chats", chatService.getChatMessagesByItem(id));

		// 出品者の平均評価を取得して、存在する場合のみ Model へ設定 
		reviewService.getAverageRatingForSeller(item.get().getSeller())
				// 小数 1 桁でフォーマットして"sellerAverageRating"として渡す 
				.ifPresent(avg -> model.addAttribute("sellerAverageRating",
						String.format("%.1f", avg)));

		// ログインユーザーがいる場合のみ、お気に入りフラグを判定 
		if (userDetails != null) {
			// ログインユーザーの User エンティティを取得
			User currentUser = userService.getUserByEmail(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));
			// 現在のユーザーがこの商品をお気に入り登録済みかどうかを判定し Model に渡す 
			model.addAttribute("isFavorited", favoriteService.isFavorited(currentUser, id));
		}
		// 商品詳細画面テンプレートを返却 
		return "item_detail";
	}

	// 新規出品フォームの表示 
	@GetMapping("/new")
	public String showAddItemForm(Model model) {
		// フォームバインド用に空の Item オブジェクトを Model へセット 
		model.addAttribute("item", new Item());
		// カテゴリ選択用にカテゴリ一覧を Model へセット 
		model.addAttribute("categories", categoryService.getAllCategories());
		// 商品登録フォームのテンプレートを返却
		return "item_form";
	}

	// 新規出品登録処理 
	@PostMapping
	public String addItem(
			// ログイン中のユーザー(出品者)情報
			@AuthenticationPrincipal UserDetails userDetails,
			// 商品名
			@RequestParam("name") String name,
			// 商品説明
			@RequestParam("description") String description,
			// 価格(BigDecimal で扱う)
			@RequestParam("price") BigDecimal price,
			// カテゴリ ID
			@RequestParam("categoryId") Long categoryId,
			// 画像ファイル(任意)
			@RequestParam(value = "image", required = false) MultipartFile imageFile,
			// リダイレクト先へメッセージを渡すためのオブジェクト
			RedirectAttributes redirectAttributes) {

		// ログインユーザーから出品者情報を取得。見つからない場合は例外 
		User seller = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Seller not found"));
		// カテゴリ ID から Category を取得。存在しなければ不正として例外
		Category category = categoryService.getCategoryById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Category not found"));

		// 新しい Item エンティティを生成してフォーム入力値をセット
		Item item = new Item();
		// 出品者を設定
		item.setSeller(seller);
		// 商品名を設定 
		item.setName(name);
		// 商品説明を設定 
		item.setDescription(description);
		// 価格を設定 
		item.setPrice(price);
		// カテゴリを設定 
		item.setCategory(category);

		try {
			// サービスに保存処理を依頼(画像アップロードも含む) 
			itemService.saveItem(item, imageFile);
			// 正常終了メッセージを Flash 属性に設定 
			redirectAttributes.addFlashAttribute("successMessage", "商品を出品しました!");
		} catch (IOException e) {
			// 画像アップロード中にエラーが発生した場合は
			// エラーメッセージを設定してフォームへ戻す 
			redirectAttributes.addFlashAttribute("errorMessage", "画像のアップロードに失敗しました: " + e.getMessage());
			return "redirect:/items/new";
		}

		// 登録完了後は商品一覧へリダイレクト 
		return "redirect:/items";
	}

	// 商品編集フォームの表示
	@GetMapping("/{id}/edit")
	public String showEditItemForm(@PathVariable("id") Long id, Model model) {
		// 編集対象の商品を取得
		Optional<Item> item = itemService.getItemById(id);
		// 存在しない場合は一覧へリダイレクト
		if (item.isEmpty()) {
			return "redirect:/items";
		}
		// 取得した Item を Model に設定
		model.addAttribute("item", item.get());
		// カテゴリ一覧も Model に設定(プルダウン用) 
		model.addAttribute("categories", categoryService.getAllCategories());
		// 新規登録と同じフォームテンプレートを再利用
		return "item_form";
	}

	// 商品編集の更新処理(POST で受け付ける。HiddenHttpMethodFilter を使えば PUT も可) 
	@PostMapping("/{id}") // Using POST for simplicity, can be PUT with HiddenHttpMethodFilter 
	public String updateItem(
			// 編集対象の商品 ID
			@PathVariable("id") Long id,
			// ログイン中のユーザー
			@AuthenticationPrincipal UserDetails userDetails,
			// 更新後の商品名
			@RequestParam("name") String name,
			// 更新後の商品説明
			@RequestParam("description") String description,
			// 更新後の価格
			@RequestParam("price") BigDecimal price,
			// 更新後のカテゴリ ID
			@RequestParam("categoryId") Long categoryId,
			// 更新時に新たな画像ファイルが指定された場合に受け取る(任意) 
			@RequestParam(value = "image", required = false) MultipartFile imageFile,
			// リダイレクトメッセージ用
			RedirectAttributes redirectAttributes) {

		// 既存の商品を ID から取得。存在しなければ例外 
		Item existingItem = itemService.getItemById(id)
				.orElseThrow(() -> new RuntimeException("Item not found"));

		// ログイン中のユーザーを取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// 編集対象商品の出品者 ID とログインユーザーID を比較し、一致しなければ編集不可 
		if (!existingItem.getSeller().getId().equals(currentUser.getId())) {
			// 出品者以外が編集しようとした場合はエラーメッセージを表示
			// Only seller can edit their item 
			redirectAttributes.addFlashAttribute("errorMessage", "この商品は編集できません。");
			return "redirect:/items";
		}

		// カテゴリ ID から Category を取得し、存在しなければ例外 
		Category category = categoryService.getCategoryById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Category not found"));

		// 既存の商品エンティティにフォームからの値を上書き 
		existingItem.setName(name);
		existingItem.setDescription(description);
		existingItem.setPrice(price);
		existingItem.setCategory(category);

		try {
			// 更新保存(画像更新を含む)を実行 
			itemService.saveItem(existingItem, imageFile);
			// 正常終了メッセージを Flash 属性に設定 
			redirectAttributes.addFlashAttribute("successMessage", "商品を更新しました!");
		} catch (IOException e) {
			// 画像アップロードエラー時はエラーメッセージを付けて編集画面に戻す
			redirectAttributes.addFlashAttribute("errorMessage",
					"画像のアップロードに失敗しました: " + e.getMessage());
			return "redirect:/items/{id}/edit";
		}
		// 更新後は対象商品の詳細画面へリダイレクト 
		return "redirect:/items/{id}";
	}

	// 商品削除処理 
	@PostMapping("/{id}/delete")
	public String deleteItem(
			// 削除対象の商品 ID
			@PathVariable("id") Long id,
			// ログイン中のユーザー
			@AuthenticationPrincipal UserDetails userDetails,
			// メッセージ用の RedirectAttributes 
			RedirectAttributes redirectAttributes) {

		// 削除対象の商品を取得
		Item itemToDelete = itemService.getItemById(id)
				.orElseThrow(() -> new RuntimeException("Item not found"));

		// ログイン中のユーザーを取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// 商品の出品者とログインユーザーが一致しなければ削除させない
		if (!itemToDelete.getSeller().getId().equals(currentUser.getId())) {
			// Only seller can delete their item 
			redirectAttributes.addFlashAttribute("errorMessage", "この商品は削除できません。");
			return "redirect:/items";
		}

		// 問題なければ商品削除処理を実行
		itemService.deleteItem(id);
		// 正常に削除できた旨のメッセージを設定 
		redirectAttributes.addFlashAttribute("successMessage", "商品を削除しました。");
		// 商品一覧へリダイレクト
		return "redirect:/items";
	}

	// 商品をお気に入りに追加する処理
	@PostMapping("/{id}/favorite")
	public String addFavorite(
			// 対象商品 ID
			@PathVariable("id") Long itemId,
			// ログインユーザー
			@AuthenticationPrincipal UserDetails userDetails,
			// メッセージ用の RedirectAttributes 
			RedirectAttributes redirectAttributes) {
		// ログインユーザーの User エンティティを取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		try {
			// お気に入り追加処理を実行 f
			avoriteService.addFavorite(currentUser, itemId);
			// 正常時メッセージを設定 
			redirectAttributes.addFlashAttribute("successMessage", "お気に入りに追加しました!");
		} catch (IllegalStateException e) {
			// すでに登録済み等のビジネス例外が発生した場合はエラーメッセージを設定
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		// 対象商品の詳細画面へ戻る 
		return "redirect:/items/{id}";
	}

	// 商品のお気に入り登録を解除する処理 
	@PostMapping("/{id}/unfavorite")
	public String removeFavorite(
			// 対象商品 ID
			@PathVariable("id") Long itemId,
			// ログインユーザー
			@AuthenticationPrincipal UserDetails userDetails,
			// メッセージ用の RedirectAttributes 
			RedirectAttributes redirectAttributes) {

		// ログインユーザーを取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		try {
			// お気に入り解除処理を実行 
			favoriteService.removeFavorite(currentUser, itemId);
			// 正常時メッセージを設定 
			redirectAttributes.addFlashAttribute("successMessage",
					"お気に入りから削除しました。");
		} catch (IllegalStateException e) {
			// ビジネスロジック上のエラーが発生した場合はエラーメッセージを設定 
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		// 対象商品の詳細画面へ戻る 
		return "redirect:/items/{id}";
	}

}
