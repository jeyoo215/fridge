-- 개발 중 서버를 재시작해도 중복 저장되지 않도록, 매번 지우고 다시 넣음 (개발 전용, 운영에서는 사용 금지)
-- 주의: recipe_ingredient가 ingredient를 참조하고 있어서, ingredient보다 먼저 지워야 함
DELETE FROM recipe_ingredient;
DELETE FROM user_ingredient;
DELETE FROM ingredient;
DELETE FROM ingredient_category;

INSERT INTO ingredient_category (category_id, category_name) VALUES
  (1, '채소'), (2, '육류'), (3, '수산물'), (4, '유제품'), (5, '콩가공품'), (6, '알류');

INSERT INTO ingredient (ingredient_id, category_id, ingredient_name, default_shelf_life_days, storage_method, is_seasoning) VALUES
  (1, 1, '상추', 5, '냉장', false),
  (2, 1, '양파', 20, '실온', false),
  (3, 5, '두부', 7, '냉장', false),
  (4, 6, '계란', 14, '냉장', false);

INSERT INTO user_ingredient (user_ingredient_id, user_id, ingredient_id, quantity, unit, purchase_date, expiration_date, status, created_at) VALUES
  (1, 1, 1, 1, '봉지', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '보유중', NOW()),
  (2, 1, 2, 3, '개', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), '보유중', NOW()),
  (3, 1, 3, 1, '모', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), '보유중', NOW()),
  (4, 1, 4, 10, '개', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 DAY), '보유중', NOW());
