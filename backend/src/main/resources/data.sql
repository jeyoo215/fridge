DELETE FROM cooking_step;
DELETE FROM recipe_ingredient;
DELETE FROM recipe_tool;
DELETE FROM recipe;
DELETE FROM recipe_category;
DELETE FROM user_ingredient;
DELETE FROM ingredient;
DELETE FROM ingredient_category;


INSERT INTO ingredient_category (category_id, category_name) VALUES
  (1, '채소'), (2, '육류'), (3, '유제품'), (4, '수산물');

INSERT INTO ingredient (ingredient_id, category_id, ingredient_name, default_shelf_life_days, storage_method, is_seasoning) VALUES
  (1, 1, '상추', 5, '냉장', false),
  (2, 1, '양파', 20, '실온', false),
  (3, 3, '두부', 7, '냉장', false),
  (4, 3, '계란', 14, '냉장', false);

-- user_id=1 이라는 임시 유저가 보유한 재료 (아직 회원 기능이 없어서 유저는 실제로 존재하지 않아도 임시로 사용)
INSERT INTO user_ingredient (user_ingredient_id, user_id, ingredient_id, quantity, unit, purchase_date, expiration_date, status, created_at) VALUES
  (1, 1, 1, 1, '봉지', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '보유중', NOW()),
  (2, 1, 2, 3, '개', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), '보유중', NOW()),
  (3, 1, 3, 1, '모', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), '보유중', NOW()),
  (4, 1, 4, 10, '개', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 DAY), '보유중', NOW());


-- 레시피 카테고리
INSERT INTO recipe_category (category_id, category_name) VALUES
  (1, '한식'), (2, '양식');

-- 레시피 (기존 재료 상추/양파/두부/계란으로 만들 수 있는 것들로 구성)
INSERT INTO recipe (recipe_id, category_id, recipe_name, cooking_time_minutes, difficulty, image_url, created_at) VALUES
  (1, 1, '계란두부부침', 15, '쉬움', NULL, NOW()),
  (2, 1, '상추양파겉절이', 10, '쉬움', NULL, NOW()),
  (3, 1, '두부계란찜', 20, '보통', NULL, NOW());

-- 레시피별 필요 재료 매핑
-- recipe 1: 계란두부부침 -> 두부(3), 계란(4)
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, quantity, unit) VALUES
  (1, 1, 3, 1, '모'),
  (2, 1, 4, 2, '개');

-- recipe 2: 상추양파겉절이 -> 상추(1), 양파(2)
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, quantity, unit) VALUES
  (3, 2, 1, 5, '장'),
  (4, 2, 2, 1, '개');

-- recipe 3: 두부계란찜 -> 두부(3), 계란(4), 양파(2)
INSERT INTO recipe_ingredient (id, recipe_id, ingredient_id, quantity, unit) VALUES
  (5, 3, 3, 1, '모'),
  (6, 3, 4, 3, '개'),
  (7, 3, 2, 1, '개');

-- 조리 단계 (recipe 1: 계란두부부침 예시만 간단히)
INSERT INTO cooking_step (step_id, recipe_id, step_order, description) VALUES
  (1, 1, 1, '두부를 도톰하게 썰어 소금간을 한다.'),
  (2, 1, 2, '계란을 풀어 두부에 옷을 입힌다.'),
  (3, 1, 3, '팬에 기름을 두르고 노릇하게 부친다.');

-- 레시피별 필요 조리도구 (cooking_tool 마스터 테이블 없어서 tool_id는 임시 숫자만 사용)
INSERT INTO recipe_tool (id, recipe_id, tool_id) VALUES
  (1, 1, 1),
  (2, 1, 2),
  (3, 2, 1),
  (4, 3, 1),
  (5, 3, 3);
