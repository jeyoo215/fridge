# ml/ — 의외의 재료 조합 추천 배치 스크립트 / 레시피 카테고리 자동 분류


레시피 재료 조합을 Apriori 연관 규칙으로 분석해서, 사용자가 아직 시도해보지 않은
"의외의 재료 조합" 레시피에 추천 점수를 매겨 DB에 저장하는 Python 배치 스크립트다.

> **역할 분리**: 이 스크립트가 `combination_recommendation` 테이블에 점수를 써넣고,
> Spring(`ComboRecommendation` 엔티티)은 그 결과를 **조회만** 한다. 

## 요구사항

- Python 3.11 이상 (requirements.txt에 명시된 패키지들이 그 버전 기준으로 배포된 버전임)
- MySQL 서버가 실행 중이어야 함
- 백엔드가 최소 한 번 구동되어 `ddl-auto=update`로 `combination_recommendation` 등
  관련 테이블이 생성되어 있어야 함 (스크립트가 테이블을 만들어주지 않음, INSERT만 함)
- 별도 DB 설정 파일 없음 — `../backend/src/main/resources/application.properties`의
  `spring.datasource.*` 값을 그대로 읽어서 접속함 (`db.py` 참고). 즉 백엔드 DB 설정을
  바꾸면 이 스크립트도 자동으로 같은 설정을 따라감.

## 설치

```bash
cd ml
python -m venv venv
Windows: venv\Scripts\activate
# (macOS/Linux: source venv/bin/activate)
pip install -r requirements.txt
```

> **주의**: `requirements.txt`가 UTF-16(BOM) 인코딩으로 저장되어 있음 (Windows에서
> `pip freeze > requirements.txt`로 생성해서 그럼). `pip install -r`은 정상적으로
> 읽지만, 터미널에서 `cat`/`grep` 등으로 열어보면 글자가 깨져 보일 수 있음. 편집할
> 일이 있으면 UTF-8로 다시 저장해서 커밋하는 걸 권장.

## 실행 방법

```bash
python calculate_combo_recommendation.py
# 안 해도 백엔드 서버 시작 시 자동 실행
```

동작 순서:
1. 레시피별 재료(조미료 `is_seasoning=true`는 제외)를 "장바구니"로 묶음
2. Apriori로 재료쌍 연관 규칙(lift)을 계산
3. 사용자별로 리뷰를 남긴 레시피 = "이미 먹어본 조합"으로 간주하고 채점에서 제외
4. 안 먹어본 레시피 중 연관 규칙 lift 합이 높은 순 상위 `TOP_N_PER_USER`(=5)개를
   `combination_recommendation` 테이블에 저장 (사용자별 기존 값은 삭제 후 재삽입)

## 언제 실행해야 하나

**실시간 아님.** 매일 오전 3시에 자동실행(업데이트) / 백엔드 서버 재시작 시 시간 불문 1회 실행

## 주의사항 / 알려진 제약

- `MIN_SUPPORT=0.05`, `MIN_CONFIDENCE=0.3`은 지금처럼 데이터(레시피 수)가 적은
  상태 기준 하드코딩 값. 레시피가 적으면 연관 규칙이 아예 안 나올 수 있는데, 이땐
  `MIN_SUPPORT`를 0.02 정도로 낮춰서 테스트 (코드 상단 주석에도 명시).
- `user` 테이블의 전체 사용자에 대해 매 실행마다 전량 재계산하는 구조라, 사용자/레시피
  수가 늘어나면 느려질 수 있음 (증분 계산 없음).
- 삭제된 레시피/사용자 등에 대한 FK 정합성은 스크립트에서 별도로 체크하지 않음.
- 문서·발표 자료에는 "Apriori 연관 규칙 기반"으로 표기하기로 함 — 요구사항
  정의서의 FR-23 문구("협업 필터링 기반")와는 다르며, 이는 논의 후 의도적으로
  바꾼 것.





  ---

# 레시피 카테고리 자동 분류

재료 원문 텍스트를 TF-IDF로 벡터화한 뒤 PyCaret 분류 모델로 레시피 카테고리
(반찬/국&찌개/후식/일품/밥/기타)를 예측하는 스크립트다. 위 조합 추천 배치와는
**별도 venv**를 쓴다 (아래 요구사항 참고).

> 현재는 AutoML 설계 과제 검증용 스크립트이며, 실제 서비스 기능(레시피 등록 시
> 카테고리 자동 추천)에는 아직 연동되지 않았다.

## 요구사항

- **Python 3.10 또는 3.11** (PyCaret 3.3.2가 3.13을 지원하지 않아 위 조합 추천용
  `venv`와는 별도의 `venv_pycaret`을 사용함)
- MySQL 서버 실행 중, `recipe`/`recipe_category` 테이블에 데이터가 있어야 함

## 설치

```bash
cd ml
py -3.11 -m venv venv_pycaret
venv_pycaret\Scripts\activate
pip install pycaret pandas mysql-connector-python
```

## 실행 방법

```bash
python classify_recipe_category.py
```

`recipe_category_model.pkl`, `recipe_category_vectorizer.pkl`이 `ml/` 아래에
생성된다. 새 재료 원문으로 카테고리를 예측하려면 같은 파일의 `predict_category()`
함수를 사용.

## 언제 실행해야 하나

**수동 실행.** 학습 데이터(recipe + recipe_category)가 식약처 API 최초 수집 시
한 번만 채워지는 구조라, 레시피 데이터가 실제로 늘어나기 전까지는 다시 돌려도
결과가 거의 동일함. 아래 "스케줄러 미연동 사유" 참고.