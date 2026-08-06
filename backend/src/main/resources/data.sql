-- 개발 중 서버를 재시작해도 중복 저장되지 않도록, 매번 지우고 다시 넣음 (개발 전용, 운영에서는 사용 금지)
DELETE FROM cooking_step;
DELETE FROM recipe_ingredient;
DELETE FROM recipe;
DELETE FROM recipe_category;
DELETE FROM user_ingredient;
DELETE FROM ingredient;
DELETE FROM ingredient_category;
DELETE FROM user_tool;
DELETE FROM cooking_tool;
DELETE FROM user_allergy_ingredient;
DELETE FROM user;

-- 프론트/다른 더미 데이터가 공통으로 참조하는 임시 유저(user_id=1). 회원가입 기능은 아직 없음.
INSERT INTO user (user_id, email, password, nickname, created_at) VALUES
  (1, 'test@example.com', NULL, '테스트유저', NOW());

-- 조리도구 마스터 (마이페이지에서 사용자가 다중선택하는 목록, 개발자가 직접 시드)
INSERT INTO cooking_tool (tool_id, tool_name) VALUES
  (1, '에어프라이어'), (2, '전자레인지'), (3, '오븐'), (4, '인덕션'),
  (5, '믹서기'), (6, '냄비'), (7, '프라이팬'), (8, '찜기');

-- user_id=1의 알레르기/기피 재료 예시 (사용자가 직접 입력한다는 가정의 더미 데이터)
INSERT INTO user_allergy_ingredient (id, user_id, ingredient_name, type) VALUES
  (1, 1, '땅콩', '알레르기'),
  (2, 1, '고수', '기피');

-- user_id=1이 보유한 조리도구 예시
INSERT INTO user_tool (user_tool_id, user_id, tool_id) VALUES
  (1, 1, 6),
  (2, 1, 7);

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
