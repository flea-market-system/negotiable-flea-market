-- 依存順に DROP してクリーンスタート(開発用途)
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS favorite_item CASCADE;
DROP TABLE IF EXISTS chat CASCADE;
DROP TABLE IF EXISTS app_order CASCADE; 
DROP TABLE IF EXISTS item CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ===== users(ユーザー)テーブル作成 =====
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    line_notify_token VARCHAR(255), --追加: line notify
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    banned BOOLEAN NOT NULL DEFAULT FALSE, -- BAN 状態
 	ban_reason TEXT, -- BAN 理由
 	banned_at TIMESTAMP, -- BAN 日時
 	banned_by_admin_id INT -- BAN 実行管理者
    
);

-- ===== category(カテゴリ)テーブル作成 =====
CREATE TABLE category (
	id SERIAL PRIMARY KEY,
	name VARCHAR(50) NOT NULL UNIQUE
);

-- ===== item(商品)テーブル作成 =====
CREATE TABLE item (
	id SERIAL PRIMARY KEY,
	users_id INT NOT NULL,
	name VARCHAR(255) NOT NULL,
	description TEXT,
	price NUMERIC(10,2) NOT NULL,
	category_id INT,
	status VARCHAR(20) DEFAULT '出品中',
	image_url TEXT,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
	
	FOREIGN KEY (users_id) REFERENCES users(id),
	FOREIGN KEY (category_id) REFERENCES category(id)
);

-- ===== app_order(注文)テーブル作成 =====
CREATE TABLE app_order (
	id SERIAL PRIMARY KEY,
	item_id INT NOT NULL,
	buyer_id INT NOT NULL,
	price NUMERIC(10,2) NOT NULL,
	status VARCHAR(20) DEFAULT '購入済',
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
	payment_intent_id VARCHAR(255) UNIQUE,  -- ★ここ独断で追加したよ
	
	FOREIGN KEY (item_id) REFERENCES item(id),
	FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- ===== chat(取引チャット)テーブル作成 =====
CREATE TABLE chat (
	id SERIAL PRIMARY KEY,
	item_id INT NOT NULL,
	sender_id INT NOT NULL,
	message TEXT,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	
	FOREIGN KEY (item_id) REFERENCES item(id),
	FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- ===== favorite_item(お気に入り)テーブル作成 ===== 
CREATE TABLE favorite_item (
	id SERIAL PRIMARY KEY,
	users_id INT NOT NULL,
	item_id INT NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	UNIQUE (users_id, item_id),
	
	FOREIGN KEY (users_id) REFERENCES users(id), 
	FOREIGN KEY (item_id) REFERENCES item(id)
);

-- ===== review(評価)テーブル作成 ===== 
CREATE TABLE review (
	id SERIAL PRIMARY KEY,
	order_id INT NOT NULL UNIQUE,
	reviewer_id INT NOT NULL,
	seller_id INT NOT NULL,
	item_id INT NOT NULL,
	rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
	comment TEXT,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	
	FOREIGN KEY (order_id) REFERENCES app_order(id),
	FOREIGN KEY (reviewer_id) REFERENCES users(id),
	FOREIGN KEY (seller_id) REFERENCES users(id),
	FOREIGN KEY (item_id) REFERENCES item(id)
);

-- 通報情報（ユーザー同士）
CREATE TABLE user_complaint (
 id SERIAL PRIMARY KEY,
 reported_user_id INT NOT NULL, -- 通報されたユーザー
 reporter_user_id INT NOT NULL, -- 通報者
 reason TEXT NOT NULL, -- 理由
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (reported_user_id) REFERENCES users(id),
 FOREIGN KEY (reporter_user_id) REFERENCES users(id)
);

-- ===== パフォーマンス向上のためのインデックス =====
CREATE INDEX IF NOT EXISTS idx_item_users_id ON item(users_id);
CREATE INDEX IF NOT EXISTS idx_item_category_id ON item(category_id);
CREATE INDEX IF NOT EXISTS idx_order_item_id ON app_order(item_id);
CREATE INDEX IF NOT EXISTS idx_order_buyer_id ON app_order(buyer_id);
CREATE INDEX IF NOT EXISTS idx_chat_item_id ON chat(item_id);
CREATE INDEX IF NOT EXISTS idx_chat_sender_id ON chat(sender_id);
CREATE INDEX IF NOT EXISTS idx_fav_users_id ON favorite_item(users_id);
CREATE INDEX IF NOT EXISTS idx_fav_item_id ON favorite_item(item_id);
CREATE INDEX IF NOT EXISTS idx_review_order_id ON review(order_id);
CREATE INDEX IF NOT EXISTS idx_uc_reported ON user_complaint(reported_user_id);
CREATE INDEX IF NOT EXISTS idx_uc_reporter ON user_complaint(reporter_user_id);


	