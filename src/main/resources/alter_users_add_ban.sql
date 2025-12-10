-- 既存の users テーブルに BAN/有効列を追加する移行スクリプト
-- アカウント停止機能・BAN 履歴管理のための列を後付けで追加する DDL
ALTER TABLE public.users
 ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE, -- ログイン可否制御
 ADD COLUMN IF NOT EXISTS banned BOOLEAN NOT NULL DEFAULT FALSE, -- BAN フラグ
 ADD COLUMN IF NOT EXISTS ban_reason TEXT, -- BAN 理由
 ADD COLUMN IF NOT EXISTS banned_at TIMESTAMP, -- BAN 日時
 ADD COLUMN IF NOT EXISTS banned_by_admin_id INT; -- 処理を行った管理者 ID
-- BAN 機能の検索効率改善用インデックス
CREATE INDEX IF NOT EXISTS idx_users_banned ON public.users(banned);
-- BAN 済みユーザー抽出用
CREATE INDEX IF NOT EXISTS idx_users_banned_by ON public.users(banned_by_admin_id);
-- 管理者別 BAN 履歴検索用