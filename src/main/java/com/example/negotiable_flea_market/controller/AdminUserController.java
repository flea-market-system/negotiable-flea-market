// 管理者によるユーザー管理機能を提供するコントローラークラスを定義
// src/main/java/com/example/negotiable_flea_market/controller/AdminUserController.java 
package com.example.negotiable_flea_market.controller;

import java.util.Comparator; 
import java.util.List;


import org.springframework.security.access.prepost.PreAuthorize; 
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.bind.annotation.RequestParam;

import com.example.negotiable_flea_market.entity.User;
import com.example.negotiable_flea_market.repository.UserRepository; 
import com.example.negotiable_flea_market.service.AdminUserService;


//このクラスが Web リクエストを処理するコントローラーであることを示す 
@Controller
//このクラスが扱う URL のプレフィックスを /admin/users に固定する 
@RequestMapping("/admin/users")
//このクラス内のハンドラーメソッドは ADMIN ロールを持つユーザーのみ実行可能とする 
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
	
	// 管理者用ユーザー管理ロジックを提供するサービスを保持するフィールド 
	private final AdminUserService service;
	// ユーザー情報への DB アクセスを行うリポジトリを保持するフィールド 
	private final UserRepository users;
	
	// コンストラクタインジェクションでサービスとリポジトリを受け取る
	public AdminUserController(AdminUserService service, UserRepository users) {
		// 引数の AdminUserService をフィールドにセット 
		this.service = service;
		// 引数の UserRepository をフィールドにセット 
		this.users = users;
	}
	
	// ユーザー一覧画面の表示を担当するハンドラー(GET /admin/users) 
	@GetMapping
	public String list(@RequestParam(value = "q", required = false) String q,
			@RequestParam(value = "sort", required = false, defaultValue = "id")
			String sort,Model model) {
		// 全ユーザー一覧をサービスから取得
		List<User> list = service.listAllUsers();

		// 検索キーワードが指定されている場合のみフィルタリングを実施 
		if (StringUtils.hasText(q)) {
			// 検索キーワードを小文字に変換して大文字・小文字を区別しない検索に対応 
			String qq = q.toLowerCase();
			// ストリーム API でユーザー名またはメールアドレスに
			// 検索キーワードを含むレコードを抽出
			list = list.stream().filter(u -> (u.getName() != null &&
					u.getName().toLowerCase().contains(qq)) ||
					(u.getEmail() != null &&
					u.getEmail().toLowerCase().contains(qq))).toList();
		}
		
		// sort パラメータの値に応じてソート条件を切り替える
		list = switch (sort) {
		// 名前順ソート:null は最後に回し、大文字小文字を無視して比較
		case "name" -> list.stream().sorted(Comparator.comparing(User::getName,
		Comparator.nullsLast(String::compareToIgnoreCase))).toList(); 
		// メールアドレス順ソート:同様に null を最後、大文字小文字無視
		case "email" -> list.stream().sorted(Comparator.comparing(User::getEmail,
				Comparator.nullsLast(String::compareToIgnoreCase))).toList();
		// BAN フラグ順ソート:BAN されているユーザーを優先表示するため降順(true が先) 
		case "banned" -> list.stream() 
		.sorted(Comparator.comparing(User::isBanned).reversed()).toList();
		// デフォルトはソートなし(取得した順のまま)
		default -> list; 
		};
		
		// 画面に表示するユーザー一覧を Model に格納 
		model.addAttribute("users", list);
		// 検索キーワードを再表示用に Model に格納 
		model.addAttribute("q", q);
		// 現在のソート条件も画面側で利用できるよう Model に格納 
		model.addAttribute("sort", sort);
		// ユーザー一覧画面に対応するテンプレートを返却
		return "admin/users/list";
	}
	
	// 個別ユーザー詳細画面の表示を担当するハンドラー(GET /admin/users/{id}) 
	@GetMapping("/{id}")
	public String detail(@PathVariable Long id, Model model) {
		// 指定 ID のユーザー情報をサービスから取得
		User user = service.findUser(id);
		// 指定ユーザーの平均評価値を取得
		Double avg = service.averageRating(id);
		// 指定ユーザーに対するクレーム件数を取得
		long complaints = service.complaintCount(id);
		// ユーザー情報を画面表示用に Model に格納 
		model.addAttribute("user", user);
		// 平均評価を Model に格納
		model.addAttribute("avgRating", avg);
		// クレーム件数を Model に格納 
		model.addAttribute("complaintCount", complaints);
		// クレーム詳細一覧を Model に格納 
		model.addAttribute("complaints", service.complaints(id)); 
		// ユーザー詳細画面に対応するテンプレート名を返却
		return "admin/users/detail";
	}
	
	// ユーザーを BAN(利用停止)する処理を担当するハンドラー(POST /admin/users/{id}/ban) 
	@PostMapping("/{id}/ban")
	public String ban(@PathVariable Long id,
			@RequestParam("reason") String reason,
			@RequestParam(value = "disableLogin", defaultValue = "true") 
			boolean disableLogin,
			Authentication auth) {
		// 認証情報から現在ログイン中の管理者のメールアドレスを取得し、
		// 対応する管理者ユーザーID を取得
		Long adminId = users.findByEmailIgnoreCase(auth.getName()).map(User::getId).orElse(null);
		// 対象ユーザーを BAN し、その操作を行った管理者 ID・理由・ログイン停止フラグを渡す 
		service.banUser(id, adminId, reason, disableLogin);
		// 対象ユーザー詳細画面へリダイレクトし、クエリパラメータで BAN 済みであることを通知 
		return "redirect:/admin/users/" + id + "?banned";
	}
	// ユーザーの BAN を解除する処理を担当するハンドラー(POST /admin/users/{id}/unban) 
	@PostMapping("/{id}/unban")
	public String unban(@PathVariable Long id) {
		// 指定ユーザーの BAN 状態を解除するようサービスに依頼
		service.unbanUser(id);
		// 対象ユーザー詳細画面へリダイレクトし、クエリパラメータで解除済みであることを通知 
		return "redirect:/admin/users/" + id + "?unbanned";
	}
}













