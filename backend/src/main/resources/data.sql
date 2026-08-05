-- schema.sql이 앱 실행할 때마다 모든 테이블을 초기화하므로, 테스트용 유저/재료도 매번 여기서 같이 넣어줌 (개발 전용)

INSERT INTO user (user_id, email, password, nickname) VALUES
  (1, 'test@test.com', '1234', '테스트유저');

INSERT INTO ingredient_category (category_id, category_name) VALUES
  (1, '채소'), (2, '육류'), (3, '유제품'), (4, '수산물');

INSERT INTO ingredient (ingredient_id, category_id, ingredient_name, default_shelf_life_days, storage_method, is_seasoning) VALUES
  (1, 1, '상추', 5, '냉장', false),
  (2, 1, '양파', 20, '실온', false),
  (3, 3, '두부', 7, '냉장', false),
  (4, 3, '계란', 14, '냉장', false);

-- user_id=1 테스트 유저가 보유한 재료 (schema.sql 기준: status 대신 is_consumed 사용)
INSERT INTO user_ingredient (user_ingredient_id, user_id, ingredient_id, quantity, unit, purchase_date, expiration_date, is_consumed) VALUES
  (1, 1, 1, 1, '봉지', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), false),
  (2, 1, 2, 3, '개', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), false),
  (3, 1, 3, 1, '모', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), false),
  (4, 1, 4, 10, '개', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 DAY), false);
