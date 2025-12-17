INSERT INTO users (name, email, password, role, enabled)
VALUES 
-- 出品者：メールとパスワード
 ('出品者 A', 'sellerA@example.com', '{noop}password', 'USER',TRUE),
 -- 購入者：わかりやすいメールに修正（'z'は誤りだと運用上混乱するため）
 ('購入者 B', 'buyerB@example.com', '{noop}password', 'USER',TRUE),
 -- 管理者：管理用アカウント
 ('運営者 C', 'adminC@example.com', '{noop}adminpass', 'ADMIN',TRUE);

 -- 初期カテゴリー
 INSERT INTO category(name) VALUES
 ('本'),
 ('家電'),
 ('ファッション'),
 ('おもちゃ'),
 ('文房具');
 
 
 -- 初期商品投入（出品者 A が 2 商品を出品）
INSERT INTO item (users_id, name, description, price, category_id, status, image_url)
VALUES
 -- Java 入門書（カテゴリ：本、出品中）
 (
 (SELECT id FROM users WHERE email = 'sellerA@example.com'),
 'Java プログラミング入門',
 '初心者向けの Java 入門書です。',
 1500.00,
 (SELECT id FROM category WHERE name = '本'),
 '出品中',
 NULL
 ),
 -- イヤホン（カテゴリ：家電、出品中）
 (
 (SELECT id FROM users WHERE email = 'sellerA@example.com'),
 'ワイヤレスイヤホン',
 'ノイズキャンセリング機能付き。',
 8000.00,
 (SELECT id FROM category WHERE name = '家電'),
 '出品中',
 NULL
 );
 
 