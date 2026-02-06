
INSERT INTO users (name, email, password, role, line_user_id, enabled) VALUES
-- 出品者 A
('出品者 A', 'sellerA@example.com', '{noop}password', 'USER', 'U2ff72e8b994e706e9e0b3c112d3a6a44', TRUE),

-- 購入者 B (ここにあなたのIDをセット)
('購入者 B', 'buyerB@example.com', '{noop}password', 'USER', 'U2ff72e8b994e706e9e0b3c112d3a6a44', TRUE),

-- 管理者 C
('運営者 C', 'adminC@example.com', '{noop}adminpass', 'ADMIN', 'U2ff72e8b994e706e9e0b3c112d3a6a44', TRUE),

-- 管理者 D
('運営者 D', 'adminD@example.com', '{noop}adminpass', 'ADMIN', 'U1d6b6bae7cd2a6559ec7e75280c8b389', TRUE);

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
 '/images/book_gijutsusyo_ai.png'
 ),
 -- イヤホン（カテゴリ：家電、出品中）
 (
 (SELECT id FROM users WHERE email = 'sellerA@example.com'),
 'ワイヤレスイヤホン',
 'ノイズキャンセリング機能付き。',
 8000.00,
 (SELECT id FROM category WHERE name = '家電'),
 '出品中',
 '/images/music_earphone_true_wireless_case.png'
 ),
  -- くまのぬいぐるみ（カテゴリ：おもちゃ、出品中）
 (
 (SELECT id FROM users WHERE email = 'sellerA@example.com'),
 'くまのぬいぐるみ',
 '多少のシミがありますので、ご了承ください。',
 2000.00,
 (SELECT id FROM category WHERE name = 'おもちゃ'),
 '出品中',
 '/images/nuigurumi_bear.png'
 ),
  -- トレッキングシューズ（カテゴリ：ファッション、出品中）
 (
 (SELECT id FROM users WHERE email = 'sellerA@example.com'),
 'トレッキングシューズ',
 '新品未使用品です。',
 13000.00,
 (SELECT id FROM category WHERE name = 'ファッション'),
 '出品中',
 '/images/tozan_kutsu.png'
 );
 
 