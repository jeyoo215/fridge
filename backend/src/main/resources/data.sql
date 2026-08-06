-- 개발 중 서버를 재시작해도 중복 저장되지 않도록, 매번 지우고 다시 넣음 (개발 전용, 운영에서는 사용 금지)
DELETE FROM recipe_ingredient;
DELETE FROM user_ingredient;
DELETE FROM ingredient;
DELETE FROM ingredient_category;

-- VisionLabelTranslator의 영문→한글 매핑 40개 전부에 맞춰서 재료 마스터를 채움
-- (카메라 인식 테스트가 이 목록 안에서만 정상적으로 매칭됨)
INSERT INTO ingredient_category (category_id, category_name) VALUES
  (1, '채소'), (2, '육류'), (3, '수산물'), (4, '유제품'), (5, '콩가공품'), (6, '알류'), (7, '과일'), (8, '곡물/가공식품');

INSERT INTO ingredient (ingredient_id, category_id, ingredient_name, default_shelf_life_days, storage_method, is_seasoning) VALUES
  -- 채소
  (1, 1, '상추', 5, '냉장', false),
  (2, 1, '양파', 20, '실온', false),
  (3, 1, '토마토', 7, '냉장', false),
  (4, 1, '감자', 30, '실온', false),
  (5, 1, '당근', 21, '냉장', false),
  (6, 1, '양배추', 14, '냉장', false),
  (7, 1, '마늘', 60, '실온', true),
  (8, 1, '오이', 7, '냉장', false),
  (9, 1, '고추', 10, '냉장', false),
  (10, 1, '피망', 10, '냉장', false),
  (11, 1, '버섯', 5, '냉장', false),
  (12, 1, '시금치', 5, '냉장', false),
  (13, 1, '브로콜리', 7, '냉장', false),
  -- 과일
  (14, 7, '사과', 30, '냉장', false),
  (15, 7, '바나나', 5, '실온', false),
  (16, 7, '오렌지', 14, '냉장', false),
  (17, 7, '레몬', 21, '냉장', false),
  (18, 7, '포도', 7, '냉장', false),
  (19, 7, '딸기', 3, '냉장', false),
  (20, 7, '수박', 10, '냉장', false),
  -- 유제품
  (21, 4, '우유', 7, '냉장', false),
  (22, 4, '치즈', 21, '냉장', false),
  (23, 4, '요거트', 14, '냉장', false),
  (24, 4, '버터', 60, '냉장', false),
  -- 육류
  (25, 2, '소고기', 5, '냉장', false),
  (26, 2, '돼지고기', 5, '냉장', false),
  (27, 2, '닭고기', 3, '냉장', false),
  (28, 2, '소시지', 14, '냉장', false),
  (29, 2, '베이컨', 14, '냉장', false),
  -- 수산물
  (30, 3, '생선', 2, '냉장', false),
  (31, 3, '새우', 2, '냉동', false),
  (32, 3, '오징어', 2, '냉동', false),
  (33, 3, '게', 2, '냉동', false),
  -- 콩가공품 / 알류
  (34, 5, '두부', 7, '냉장', false),
  (35, 6, '계란', 14, '냉장', false),
  -- 곡물/가공식품
  (36, 8, '쌀', 180, '실온', false),
  (37, 8, '빵', 5, '실온', false),
  (38, 8, '면', 90, '실온', false);

-- 테스트용 보유 재료 (유통기한 다양하게 섞어서 D-day 색상 구분도 같이 확인 가능)
INSERT INTO user_ingredient (user_ingredient_id, user_id, ingredient_id, quantity, unit, purchase_date, expiration_date, status, created_at) VALUES
  (1, 1, 1, 1, '봉지', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '보유중', NOW()),
  (2, 1, 2, 3, '개', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), '보유중', NOW()),
  (3, 1, 34, 1, '모', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), '보유중', NOW()),
  (4, 1, 35, 10, '개', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 DAY), '보유중', NOW());
