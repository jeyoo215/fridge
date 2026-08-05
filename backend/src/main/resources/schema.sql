-- =========================================================
-- 냉장고 파먹기 (Fridge to Table) - 초기 스키마
-- 위치: backend/src/main/resources/schema.sql
-- 생성일: 2026-08-05
-- 규칙:
--   - 모든 테이블: InnoDB, utf8mb4
--   - PK는 BIGINT AUTO_INCREMENT
--   - 대부분 테이블에 created_at / updated_at 부여 (BaseEntity 매핑용)
--   - FK는 참조 무결성을 위해 명시, 유저 소유 데이터는 ON DELETE CASCADE
--   - spring.sql.init.mode=always 로 앱 실행 시마다 재실행되므로,
--     아래 DROP 구문으로 매번 초기화 후 재생성함 (로컬 개발 전용, 데이터는 매번 초기화됨)
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------
-- 기존 테이블 정리 (생성 순서의 역순으로 DROP)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS file_attachment;
DROP TABLE IF EXISTS image_recognition_log;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS streak_record;
DROP TABLE IF EXISTS user_badge;
DROP TABLE IF EXISTS recipe_review;
DROP TABLE IF EXISTS challenge_participation;
DROP TABLE IF EXISTS waste_prevention_stat;
DROP TABLE IF EXISTS carbon_reduction_record;
DROP TABLE IF EXISTS monthly_saving_stat;
DROP TABLE IF EXISTS shopping_list_item;
DROP TABLE IF EXISTS shopping_list;
DROP TABLE IF EXISTS combo_recommendation;
DROP TABLE IF EXISTS user_preference;
DROP TABLE IF EXISTS ingredient_priority_score;
DROP TABLE IF EXISTS recommendation_history;
DROP TABLE IF EXISTS cooking_step;
DROP TABLE IF EXISTS recipe_tool;
DROP TABLE IF EXISTS recipe_ingredient;
DROP TABLE IF EXISTS recipe;
DROP TABLE IF EXISTS storage_guide;
DROP TABLE IF EXISTS ingredient_consumption_log;
DROP TABLE IF EXISTS user_ingredient;
DROP TABLE IF EXISTS user_allergy_ingredient;
DROP TABLE IF EXISTS ingredient;
DROP TABLE IF EXISTS user_tool;
DROP TABLE IF EXISTS notification_setting;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS social_account;
DROP TABLE IF EXISTS challenge;
DROP TABLE IF EXISTS badge;
DROP TABLE IF EXISTS recipe_category;
DROP TABLE IF EXISTS ingredient_category;
DROP TABLE IF EXISTS cooking_tool;
DROP TABLE IF EXISTS user;

-- =========================================================
-- 1. 회원 / 인증
-- =========================================================

CREATE TABLE user (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NULL,               -- 소셜 로그인만 쓰는 경우 NULL 허용
    nickname        VARCHAR(50)  NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cooking_tool (
    tool_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_name       VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ingredient_category (
    category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name   VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recipe_category (
    category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name   VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE badge (
    badge_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    badge_name      VARCHAR(100) NOT NULL,
    description     VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE challenge (
    challenge_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     TEXT NULL,
    start_date      DATE NULL,
    end_date        DATE NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE social_account (
    social_account_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT NOT NULL,
    provider           VARCHAR(30) NOT NULL,          -- GOOGLE, KAKAO, NAVER 등
    provider_user_id   VARCHAR(255) NOT NULL,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_social_account_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_social_account_provider UNIQUE (provider, provider_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_token (
    token_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    token           VARCHAR(500) NOT NULL,
    expires_at      DATETIME NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification_setting (
    setting_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    expiration_alert_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    alert_days_before       INT NOT NULL DEFAULT 3,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_setting_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_tool (
    user_tool_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    tool_id         BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_tool_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_tool_tool FOREIGN KEY (tool_id) REFERENCES cooking_tool(tool_id) ON DELETE CASCADE,
    CONSTRAINT uq_user_tool UNIQUE (user_id, tool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 2. 재료
-- =========================================================

CREATE TABLE ingredient (
    ingredient_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id             BIGINT NULL,                       -- 페어1 엔티티가 @JoinColumn(nullable 기본값=true)라 NULL 허용
    ingredient_name         VARCHAR(50) NOT NULL,
    default_shelf_life_days INT NULL,
    storage_method          VARCHAR(20) NULL,                  -- Enum(STRING) 저장: '냉장', '냉동', '실온'
    is_seasoning            BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ingredient_category FOREIGN KEY (category_id) REFERENCES ingredient_category(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_alergy_ingredient (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    ingredient_id   BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_allergy_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_allergy_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id) ON DELETE CASCADE,
    CONSTRAINT uq_user_allergy UNIQUE (user_id, ingredient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_ingredient (
    user_ingredient_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    ingredient_id       BIGINT NOT NULL,
    quantity            DECIMAL(10,2) NOT NULL DEFAULT 1,
    unit                VARCHAR(20) NULL,
    purchase_date       DATE NULL,
    expiration_date     DATE NULL,
    is_consumed         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_ingredient_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_ingredient_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ingredient_consumption_log (
    log_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_ingredient_id  BIGINT NOT NULL,
    consumed_quantity   DECIMAL(10,2) NOT NULL,
    consumed_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_consumption_log_user_ingredient FOREIGN KEY (user_ingredient_id) REFERENCES user_ingredient(user_ingredient_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE storage_guide (
    guide_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id   BIGINT NOT NULL,
    storage_method  TEXT NULL,
    is_freezable    BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_storage_guide_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 3. 레시피
-- =========================================================

CREATE TABLE recipe (
    recipe_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id          BIGINT NOT NULL,
    recipe_name           VARCHAR(200) NOT NULL,
    cooking_time_minutes  INT NULL,
    difficulty            VARCHAR(20) NULL,             -- EASY, MEDIUM, HARD
    image_url             VARCHAR(500) NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recipe_category FOREIGN KEY (category_id) REFERENCES recipe_category(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recipe_ingredient (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id       BIGINT NOT NULL,
    ingredient_id   BIGINT NOT NULL,
    quantity        DECIMAL(10,2) NULL,
    unit            VARCHAR(20) NULL,
    CONSTRAINT fk_recipe_ingredient_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_ingredient_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recipe_tool (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id       BIGINT NOT NULL,
    tool_id         BIGINT NOT NULL,
    CONSTRAINT fk_recipe_tool_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_tool_tool FOREIGN KEY (tool_id) REFERENCES cooking_tool(tool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cooking_step (
    step_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id       BIGINT NOT NULL,
    step_order      INT NOT NULL,
    description     TEXT NOT NULL,
    CONSTRAINT fk_cooking_step_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 4. 추천 / AutoML
-- =========================================================

CREATE TABLE recommendation_history (
    recommendation_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    recipe_id           BIGINT NOT NULL,
    recommended_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason              VARCHAR(255) NULL,
    CONSTRAINT fk_recommendation_history_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_recommendation_history_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ingredient_priority_score (
    score_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_ingredient_id  BIGINT NOT NULL,
    score               DECIMAL(6,3) NOT NULL,
    calculated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_priority_score_user_ingredient FOREIGN KEY (user_ingredient_id) REFERENCES user_ingredient(user_ingredient_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_preference (
    preference_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    recipe_category_id  BIGINT NOT NULL,
    preference_score    DECIMAL(6,3) NOT NULL DEFAULT 0,
    CONSTRAINT fk_user_preference_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_preference_category FOREIGN KEY (recipe_category_id) REFERENCES recipe_category(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE combo_recommendation (
    combo_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id           BIGINT NOT NULL,
    user_id             BIGINT NOT NULL,
    combo_description   VARCHAR(255) NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_combo_recommendation_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    CONSTRAINT fk_combo_recommendation_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 5. 장보기
-- =========================================================

CREATE TABLE shopping_list (
    list_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shopping_list_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE shopping_list_item (
    item_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    list_id         BIGINT NOT NULL,
    ingredient_id   BIGINT NOT NULL,
    quantity        DECIMAL(10,2) NULL,
    unit            VARCHAR(20) NULL,
    is_purchased    BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_shopping_list_item_list FOREIGN KEY (list_id) REFERENCES shopping_list(list_id) ON DELETE CASCADE,
    CONSTRAINT fk_shopping_list_item_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 6. 통계
-- =========================================================

CREATE TABLE monthly_saving_stat (
    stat_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    `year_month`      VARCHAR(7) NOT NULL,               -- 'YYYY-MM'
    saved_amount    DECIMAL(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_monthly_saving_stat_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_monthly_saving_stat UNIQUE (user_id, `year_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE carbon_reduction_record (
    record_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT NOT NULL,
    reduced_amount_kg    DECIMAL(10,3) NOT NULL DEFAULT 0,
    recorded_at          DATE NOT NULL,
    CONSTRAINT fk_carbon_reduction_record_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE waste_prevention_stat (
    stat_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT NOT NULL,
    prevented_weight_kg  DECIMAL(10,3) NOT NULL DEFAULT 0,
    `year_month`           VARCHAR(7) NOT NULL,
    CONSTRAINT fk_waste_prevention_stat_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_waste_prevention_stat UNIQUE (user_id, `year_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 7. 커뮤니티 / 게이미피케이션
-- =========================================================

CREATE TABLE challenge_participation (
    participation_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    challenge_id        BIGINT NOT NULL,
    user_id             BIGINT NOT NULL,
    joined_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, SUCCESS, FAILED
    CONSTRAINT fk_challenge_participation_challenge FOREIGN KEY (challenge_id) REFERENCES challenge(challenge_id) ON DELETE CASCADE,
    CONSTRAINT fk_challenge_participation_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_challenge_participation UNIQUE (challenge_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE recipe_review (
    review_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id       BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    rating          TINYINT NOT NULL,                  -- 1~5
    content         TEXT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recipe_review_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_review_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_recipe_review_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_badge (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    badge_id        BIGINT NOT NULL,
    earned_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_badge_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_badge_badge FOREIGN KEY (badge_id) REFERENCES badge(badge_id) ON DELETE CASCADE,
    CONSTRAINT uq_user_badge UNIQUE (user_id, badge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE streak_record (
    streak_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    current_streak       INT NOT NULL DEFAULT 0,
    longest_streak        INT NOT NULL DEFAULT 0,
    last_active_date      DATE NULL,
    CONSTRAINT fk_streak_record_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_streak_record_user UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 8. 시스템
-- =========================================================

CREATE TABLE notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    type            VARCHAR(30) NOT NULL,              -- EXPIRATION, CHALLENGE, REVIEW 등
    message         VARCHAR(255) NOT NULL,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE image_recognition_log (
    log_id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    recognized_ingredient_id BIGINT NULL,               -- 인식 실패 시 NULL 허용
    image_url               VARCHAR(500) NOT NULL,
    confidence_score         DECIMAL(5,4) NULL,
    created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_image_recognition_log_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_image_recognition_log_ingredient FOREIGN KEY (recognized_ingredient_id) REFERENCES ingredient(ingredient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE file_attachment (
    file_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    related_type    VARCHAR(50) NOT NULL,               -- 'RECIPE', 'RECIPE_REVIEW', 'USER_INGREDIENT' 등 (polymorphic, FK 제약 없음)
    related_id      BIGINT NOT NULL,
    file_url        VARCHAR(500) NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;