package com.example.negotiable_flea_market.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.negotiable_flea_market.service.AppOrderService;
import com.example.negotiable_flea_market.service.ItemService;

//このクラスが Web リクエストを処理する「コントローラ」であることを示すアノテーション
@Controller
//このクラスで扱う URL の共通プレフィックス(/admin 配下の URL を担当)
@RequestMapping("/admin")
//このクラスの全メソッドを実行するには ADMIN ロールが必要であることを指定 
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
	// 商品関連のビジネスロジックを扱うサービスクラスのフィールド
	private final ItemService itemService;
	// 注文・売上などアプリ全体の注文情報を扱うサービスクラスのフィールド 
	private final AppOrderService appOrderService;

	// コンストラクタインジェクションにより、サービスを受け取ってフィールドに設定
	public AdminController(ItemService itemService, AppOrderService appOrderService) {
		// 引数で受け取った ItemService をフィールドに格納 
		this.itemService = itemService;
		// 引数で受け取った AppOrderService をフィールドに格納 
		this.appOrderService = appOrderService;
	}

	// 管理者向けの商品一覧画面を表示するハンドラ(GET /admin/items) 
	@GetMapping("/items")
	public String manageItems(Model model) {
		// すべての商品一覧を取得して、ビューに渡すために Model へ登録
		model.addAttribute("items", itemService.getAllItems());
		// admin_items.html というテンプレート名を返し、商品管理画面を表示 r
		eturn "admin_items";
}

	// 管理者が商品を削除するためのハンドラ(POST /admin/items/{id}/delete) 
	@PostMapping("/items/{id}/delete")
	public String deleteItemByAdmin(@PathVariable("id") Long itemId) {
		// パスから取得した商品 ID を使って商品削除処理をサービスに依頼 
		itemService.deleteItem(itemId);
		// 削除成功後、商品一覧画面へリダイレクトし、クエリパラメータで成功メッセージを付加 
		return "redirect:/admin/items?success=deleted";
	}

	// 売上統計を表示する画面用ハンドラ(GET /admin/statistics) 
	@GetMapping("/statistics")
	public String showStatistics(
			// 開始日をクエリパラメータから取得(任意)。指定が無い場合は null になる 
			@RequestParam(value = "startDate", required = false)
			// 文字列の日付を ISO 形式(yyyy-MM-dd)として LocalDate に変換する指定 
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			// 終了日をクエリパラメータから取得(任意)。指定が無い場合は null になる 
			@RequestParam(value = "endDate", required = false)
			// 同様に、ISO 形式で LocalDate に変換する指定
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			// 画面に渡す値を詰め込むための Model オブジェクト 
			Model model) {
		// startDate が指定されなかった場合、デフォルトで「1 ヶ月前の日付」を開始日に設定 
		if (startDate == null)
			startDate = LocalDate.now().minusMonths(1);
		// endDate が指定されなかった場合、デフォルトで「本日」を終了日に設定 
		if (endDate == null)
			endDate = LocalDate.now();
		// 画面側で選択状態を表示できるように、開始日を Model に登録 
		model.addAttribute("startDate", startDate);
		// 終了日も同様に Model に登録
		model.addAttribute("endDate", endDate);
		// 指定期間の総売上金額をサービスから取得し、画面表示用に Model へ登録 
		model.addAttribute("totalSales",
				appOrderService.getTotalSales(startDate, endDate));
		// 指定期間のステータス別注文数(例:完了・キャンセルなど)を取得し Model へ登録 
		model.addAttribute("orderCountByStatus", appOrderService.getOrderCountByStatus(startDate, endDate));
		// admin_statistics.html というテンプレート名を返し、統計画面を表示
		return "admin_statistics";
	}

	// 売上統計を CSV 形式でダウンロードさせるためのハンドラ(GET /admin/statistics/csv)
	@GetMapping("/statistics/csv")
	public void exportStatisticsCsv(
			// CSV 出力用の開始日パラメータ(任意)。
			// 指定がなくても動作するように required=false
			@RequestParam(value = "startDate", required = false)
			// 日付文字列を LocalDate に変換するフォーマット指定 
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			// CSV 出力用の終了日パラメータ(任意)
			@RequestParam(value = "endDate", required = false)
			// 同様に ISO 形式で LocalDate に変換
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			// HTTP レスポンス(ヘッダ設定や出力ストリーム取得に使用) 
			HttpServletResponse response) throws IOException {

		// 開始日が null なら、デフォルトで 1 ヶ月前に設定(画面表示時と同じロジック) 
		if (startDate == null)
			startDate = LocalDate.now().minusMonths(1);
		// 終了日が null なら、デフォルトで本日に設定
		if (endDate == null)
			endDate = LocalDate.now();

		// レスポンスのコンテンツタイプを CSV(UTF-8)としてクライアントに通知 
		response.setContentType("text/csv; charset=UTF-8");
		// ブラウザに「ファイルとしてダウンロードさせる」
		// ためのヘッダを設定(ファイル名も指定) 
		response.setHeader("Content-Disposition", "attachment;filename=\"flea_market_statistics.csv\"");

		// try-with-resources 構文で PrintWriter を取得し、自動でクローズさせる 
		try (PrintWriter writer = response.getWriter()) {
			// 統計期間の情報を 1 行目に出力
			writer.append("統計期間: ").append(String.valueOf(startDate)).append(" から ").append(String.valueOf(endDate))
					.append("\n\n");
			// 2 ブロック目として、期間内の総売上を出力
			writer.append("総売上: ").append(String.valueOf(appOrderService.getTotalSales(startDate, endDate)))
					.append("\n\n");
			// ステータス別注文数のヘッダ行を出力
			writer.append("ステータス別注文数\n");
			// ステータスごとの件数マップを取り出し、
			// 1 行ずつ「ステータス,件数」の形式で出力
			appOrderService.getOrderCountByStatus(startDate, endDate)
					.forEach((status, count) -> writer.append(status).append(",").append(String.valueOf(count))
							.append("\n"));
		}
	}
}