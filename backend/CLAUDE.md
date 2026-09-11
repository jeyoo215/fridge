# 냉장고 파먹기 (Fridge to Table) - Backend

보유 냉장고 재료를 등록하면 만들 수 있는 레시피를 추천하고, 유통기한 임박 재료를 우선 소진하도록 유도해
음식물 쓰레기와 식비 낭비를 줄이는 웹 서비스.

## 기술 스택
- Backend: Spring Boot 4.1.0 (Gradle), Java 17
- DB: MySQL 8.x (스키마명 `fridge`)
- Frontend: React (별도 `frontend/` 참고)
- 배포: AWS EC2 + Docker (후반부)
- 추천: 초기엔 규칙 기반 스코어링, 이후 AutoML로 재정렬

## 팀 구성
- 4인, 2인 1조 페어.
- **페어 1** 담당: 회원/재료 관리 (`domain.ingredient`) → 이후 카메라 인식(Vision API) / 배포
- **페어 2** 담당: 레시피 추천(AutoML) (`domain.recipe`) → 이후 장보기 / 통계 / 커뮤니티·챌린지

## 프로젝트 구조 (도메인형 패키지)
베이스 패키지: `com.example.backend`
```
backend/src/main/java/com/example/backend/
├── config/                 # 공통 설정 (WebConfig 등)
├── controller/              # 공통 컨트롤러 (HealthCheck 등)
├── domain/
│   ├── ingredient/          # (페어1) 재료 마스터, 사용자 보유 재료
│   └── recipe/               # (페어2) 레시피 도메인 (Recipe, RecipeCategory, RecipeIngredient, RecipeTool, CookingStep)
└── BackendApplication.java
```
각 도메인 폴더 안에 엔티티·컨트롤러·서비스·리포지토리·dto를 함께 둔다. 도메인별로 이 패턴을 지킬 것.

## 코딩 컨벤션 (엔티티) — 반드시 준수
`domain/ingredient` 코드가 기준 예시.
- 클래스: `@Entity`, `@Table(name = "snake_case")`, `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
- `@Setter`는 쓰지 않는다. 값 변경은 의미 있는 메서드로 (예: `consume()`, `updateQuantityAndExpiration(...)`)
- 생성자 위에 `@Builder`를 붙인다 (클래스 레벨 X)
- PK: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`, 타입 `Long`, `@Column(name = "...")`로 명시
- 모든 컬럼은 `@Column(name = "snake_case")`로 명시 (필드는 camelCase)
- 연관관계: `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn(name = "...")`
- enum: `@Enumerated(EnumType.STRING)`, 상수는 **한글**로 (예: `냉장/냉동/실온`, `보유중/소진/폐기`, `쉬움/보통/어려움`)
- `created_at`은 생성자에서 `LocalDateTime.now()`로 세팅

## DB 초기화 규칙 (중요)
- `application.properties`에 `spring.jpa.hibernate.ddl-auto=update`가 켜져 있음.
- **테이블은 엔티티가 만든다.** `resources/schema.sql`에는 CREATE TABLE을 넣지 않는다 (엔티티와 이중 정의되어 충돌하므로 비워둠).
  - 전체 ERD 설계 참고 자료는 [docs/erd-reference.sql](../docs/erd-reference.sql)에 보관 (실행용 아님).
- `data.sql`은 시드 데이터 전용. `defer-datasource-initialization=true`라 테이블 생성 후 실행됨. INSERT 컬럼명은 엔티티가 만든 컬럼명과 정확히 일치해야 함.

## 회원 참조 임시 규칙
- 아직 `User` 엔티티가 없음. 사용자 참조는 임시로 `Long userId` 컬럼(`@Column(name="user_id")`)만 저장하고, `// TODO: User 엔티티 생기면 @ManyToOne 으로 교체` 주석을 남긴다.

## 페어 2 도메인 (ERD 기준, `domain/recipe`에 구현)
### 레시피
- `recipe` (recipe_id PK, category_id FK, recipe_name, cooking_time_min, difficulty, servings, image_url, source, created_at)
- `recipe_category` (category_id PK, category_name) — 평면 구조, parent 없음
- `recipe_ingredient` (id PK, recipe_id FK, ingredient_id FK, is_essential, amount, unit) — `ingredient_id`는 페어1 `ingredient` 참조. **조미료도 `ingredient.is_seasoning=true`로 여기 포함**(별도 seasoning 테이블 없음). `is_essential`이 추천 매칭의 핵심.
- `recipe_tool` (id PK, recipe_id FK, tool_id FK) — `tool_id`는 페어1 `cooking_tool` 참조. FR-22용, 확장.
- `cooking_step` (step_id PK, recipe_id FK, step_order, description, image_url, duration_sec)

### 추천/AutoML
- `recommendation_history` (recommendation_id PK, user_id FK, recipe_id FK)
- `ingredient_priority_score` (score_id PK, user_ingredient_id FK) — 유통기한 임박도 점수
- `user_preference` (preference_id PK, user_id FK, recipe_category_id FK)
- `combo_recommendation` (combo_id PK, user_id FK, recipe_id FK)

## 추천 로직 방향
추천 점수(규칙 기반, AutoML 이전 단계):
`score = w1 × (보유 필수재료 / 레시피 필수재료) + w2 × Σ(매칭된 임박재료 priority_score) − w3 × 부족한 필수재료 수`
- FR-20: 보유 재료로 만들 수 있는 레시피 매칭·랭킹
- FR-21: 유통기한 임박 재료를 쓰는 레시피에 가점
- AutoML은 `recommendation_history`(클릭/조리 로그)·`user_preference`가 쌓인 뒤 재정렬 용도로 얹는다.

## 우선순위 (MVP 먼저)
1. FR-24 레시피 등록/상세 조회 (CRUD)
2. FR-20 보유 재료 기반 매칭·추천
3. FR-21 유통기한 임박 가중치 랭킹
- 확장(나중): FR-22 조미료/조리도구 필터, FR-23 의외의 조합
- 레시피 데이터는 초기엔 한식 위주로 축소해서 시작.

## Git 협업 규칙
- 작업 전 항상 `git checkout develop && git pull origin develop`
- `feature/기능이름` 브랜치에서 작업 → push → develop으로 PR
- 커밋 메시지: `feat:` `fix:` `refactor:` `docs:` `test:` `chore:` 접두어 사용
