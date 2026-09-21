# ml/ — 의외의 재료 조합 추천 배치 스크립트 / 레시피 카테고리 자동 분류

### 설치

이 스크립트는 `mlxtend`와 `pycaret`을 **같이** 써서, 반드시 `venv_pycaret`
가상환경을 쓴다 (아래 2번 항목 카테고리 분류랑 같은 venv 공유):

> **먼저 Python 3.11을 시스템에 설치해야 함.** `venv_pycaret` 폴더는 패키지만
> 담는 가상환경일 뿐, 그 안에 Python 3.11 실행 파일 자체가 들어있는 게 아니다.
> PyCaret 3.3.2가 Python 3.13을 지원하지 않아서(numpy 빌드 실패로 설치 자체가
> 안 됨) 별도로 3.11을 깔아야 하며, 이미 3.13 등 다른 버전을 쓰고 있어도 같이
> 설치해두면 된다. **설치 시 "Add python.exe to PATH" 체크는 하지 않는 것을
> 권장** — 체크하면 터미널에서 기본 `python` 명령이 3.11로 바뀌어버려 다른
> 작업에 영향을 줄 수 있다. 체크 안 해도 아래처럼 `py -3.11`로 정확히 지정해서
> 부르면 된다.

```bash
cd ml
py -3.11 -m venv venv_pycaret
Windows: venv_pycaret\Scripts\activate
# (macOS/Linux: source venv_pycaret/bin/activate)

# 반드시 2단계로 나눠서 설치할 것 (아래 경고 참고)
pip install -r requirements_pycaret.txt
pip install mlxtend==0.25.0 --no-deps
```

> **왜 2단계로 나누는가**: `requirements_pycaret.txt`에는 `mlxtend==0.25.0`과
> `scipy==1.11.4`가 같이 고정돼 있는데, `mlxtend==0.25.0`이 실제로 공식
> 요구하는 버전은 `scipy>=1.16.3`이다 (pip 메타데이터 기준 명백한 모순). 즉
> **한 번에 `pip install -r requirements_pycaret.txt`만 실행하면 pip가 이
> 모순을 감지해서 설치를 거부하거나, 해결책을 찾느라 오래 걸리거나, 예측 불가능한
> 조합으로 깔릴 수 있다.**
>
> 실제로는 `mlxtend`의 핵심 함수(`apriori`, `association_rules`,
> `TransactionEncoder`)가 그 최신 scipy 전용 기능을 쓰지 않아서, 훨씬 낮은
> `scipy==1.11.4`로도 정상 동작하는 걸 확인했다. 그래서 `pycaret` 생태계
> (scipy/numpy/scikit-learn 버전들)를 먼저 깔아서 고정시킨 다음,
> `mlxtend`는 `--no-deps`로 **의존성 버전 체크 없이 지금 깔린 환경 위에
> 그대로 얹는** 방식으로 설치한다.
>
> **이 venv에 뭔가 추가로 설치해야 할 일이 생기면, 절대 그냥 `pip install
> <패키지>`를 바로 하지 말 것.** 먼저 위 2단계 순서대로 복원한 다음에
> 추가 패키지를 설치할 것 — 그렇지 않으면 이번에 정리한 이 조합이 또 깨질 수 있음
> (mlxtend를 나중에 추가 설치했다가 scipy/scikit-learn이 자동으로 최신으로
> 올라가면서 pycaret이 통째로 로드 실패했던 사고가 이미 한 번 있었음).


# ml/ — 조합 추천 배치 (AutoML) / 레시피 카테고리 자동 분류

두 개의 독립된 Python 스크립트가 있다. **조합 추천(`calculate_combo_recommendation.py`)이
실제 서비스 기능(FR-23)이고, 카테고리 분류(`classify_recipe_category.py`)는
필수는 아니고 AutoML 설계 과제 검증용으로 남겨둔 것**이다.

---

## 1. 조합 추천 배치 (`calculate_combo_recommendation.py`)

레시피 재료 조합의 "궁합 지수"를 Apriori로 계산해 라벨로 삼고, 그 라벨을
PyCaret 회귀 모델(AutoML)로 학습시켜 모든 레시피에 예측 점수를 매긴 뒤,
사용자별 개인화(안 먹어본 조합 할인 + 보유 재료 매칭 + 유통기한 임박 가산점)를
더해 `combination_recommendation` 테이블에 저장하는 배치 스크립트다.

### 파이프라인

1. **Apriori (라벨 생성)** — 레시피별 재료(조미료 제외)를 "장바구니"로 묶어 연관
   규칙(lift)을 계산하고, 레시피마다 그 재료쌍들의 lift 합을 "궁합 지수"로 산출
2. **PyCaret 회귀 (AutoML, 핵심 엔진)** — 레시피의 영양성분·난이도·카테고리·재료
   개수만으로 그 궁합 지수를 예측하는 회귀 모델을 `compare_models()`로 여러
   알고리즘 중 자동 선택해 학습. Apriori 규칙이 안 걸리는 레시피(라벨=0)에도
   일반화된 예측치를 줘서, 순수 Apriori보다 추천 커버리지가 넓음
3. **개인화 (사용자별)** — 최종 점수 = `예측 궁합 점수 × (1-이미 먹어본 비율)
   + 보유 재료 매칭 가산점 + 유통기한 임박 가산점`. 유통기한 가산점 기준(D-1→3점,
   D-3→2점)은 `RecipeService.calculateExpiryPriorityScore()`와 동일하게 맞춤
4. 사용자별 상위 `TOP_N_PER_USER`(=20)개를 `combination_recommendation`에 저장
   (기존 값은 삭제 후 재삽입). `fully_matched`(필수 재료 완전 보유 여부) 컬럼도
   같이 써넣지만, **지금은 Java가 이 값을 안 읽고 조회 시점에 직접 재계산함**
   (아래 "역할 분리" 참고)

### 역할 분리

> - **추천 순위(`combo_score`)**: 이 스크립트가 계산해서 저장 → Spring은 **조회만**
> - **"있는 재료만 활용" 필터**: `ComboRecommendationService`가 API 호출 시점에
>   사용자의 **현재** 보유 재료로 실시간 재판정함 (배치가 저장한 `fully_matched`
>   값은 안 씀). 재료를 방금 등록해도 다음 배치(최대 하루 지연)를 안 기다리고
>   바로 필터에 반영되게 하려고 이렇게 분리함

### 요구사항

- **Python 3.10 또는 3.11** (PyCaret 3.3.2가 3.13을 지원 안 함)
- MySQL 서버 실행 중
- 백엔드가 최소 한 번 구동되어 `ddl-auto=update`로 `combination_recommendation`
  등 관련 테이블이 생성되어 있어야 함 (스크립트가 테이블을 만들어주지 않음, INSERT만 함)
- 별도 DB 설정 파일 없음 — `../backend/src/main/resources/application.properties`의
  `spring.datasource.*` 값을 그대로 읽어서 접속함 (`db.py` 참고)

### 설치

이 스크립트는 `mlxtend`와 `pycaret`을 **같이** 써서, 반드시 `venv_pycaret`
가상환경을 쓴다 (아래 2번 항목 카테고리 분류랑 같은 venv 공유):

> **먼저 Python 3.11을 시스템에 설치해야 함.** `venv_pycaret` 폴더는 패키지만
> 담는 가상환경일 뿐, 그 안에 Python 3.11 실행 파일 자체가 들어있는 게 아니다.
> PyCaret 3.3.2가 Python 3.13을 지원하지 않아서(numpy 빌드 실패로 설치 자체가
> 안 됨) 별도로 3.11을 깔아야 하며, 이미 3.13 등 다른 버전을 쓰고 있어도 같이
> 설치해두면 된다. **설치 시 "Add python.exe to PATH" 체크는 하지 않는 것을
> 권장** — 체크하면 터미널에서 기본 `python` 명령이 3.11로 바뀌어버려 다른
> 작업에 영향을 줄 수 있다. 체크 안 해도 아래처럼 `py -3.11`로 정확히 지정해서
> 부르면 된다.

```bash
cd ml
py -3.11 -m venv venv_pycaret
Windows: venv_pycaret\Scripts\activate
# (macOS/Linux: source venv_pycaret/bin/activate)

# 반드시 2단계로 나눠서 설치할 것 (아래 경고 참고)
pip install -r requirements_pycaret.txt
pip install mlxtend==0.25.0 --no-deps
```

> **왜 2단계로 나누는가**: `requirements_pycaret.txt`에는 `mlxtend==0.25.0`과
> `scipy==1.11.4`가 같이 고정돼 있는데, `mlxtend==0.25.0`이 실제로 공식
> 요구하는 버전은 `scipy>=1.16.3`이다 (pip 메타데이터 기준 명백한 모순). 즉
> **한 번에 `pip install -r requirements_pycaret.txt`만 실행하면 pip가 이
> 모순을 감지해서 설치를 거부하거나, 해결책을 찾느라 오래 걸리거나, 예측 불가능한
> 조합으로 깔릴 수 있다.**
>
> 실제로는 `mlxtend`의 핵심 함수(`apriori`, `association_rules`,
> `TransactionEncoder`)가 그 최신 scipy 전용 기능을 쓰지 않아서, 훨씬 낮은
> `scipy==1.11.4`로도 정상 동작하는 걸 확인했다. 그래서 `pycaret` 생태계
> (scipy/numpy/scikit-learn 버전들)를 먼저 깔아서 고정시킨 다음,
> `mlxtend`는 `--no-deps`로 **의존성 버전 체크 없이 지금 깔린 환경 위에
> 그대로 얹는** 방식으로 설치한다.
>
> **이 venv에 뭔가 추가로 설치해야 할 일이 생기면, 절대 그냥 `pip install
> <패키지>`를 바로 하지 말 것.** 먼저 위 2단계 순서대로 복원한 다음에
> 추가 패키지를 설치할 것 — 그렇지 않으면 이번에 정리한 이 조합이 또 깨질 수 있음
> (mlxtend를 나중에 추가 설치했다가 scipy/scikit-learn이 자동으로 최신으로
> 올라가면서 pycaret이 통째로 로드 실패했던 사고가 이미 한 번 있었음).


### 실행 방법

```bash
python calculate_combo_recommendation.py
# 특정 유저 하나만: python calculate_combo_recommendation.py 3
```

### 언제 실행되나

**실시간 아님.** 아래 트리거로 자동 실행됨 (`ComboRecommendationScheduler`):
- 매일 새벽 3시
- 백엔드 서버 시작 시 1회
- 사용자가 리뷰를 새로 남겼을 때 (그 유저만 재계산)
- 관리자가 수동으로 전체 재계산 요청했을 때

### 주의사항 / 알려진 제약

- `MIN_SUPPORT=0.05`, `MIN_CONFIDENCE=0.3`은 지금 레시피 수 기준 하드코딩 값.
  연관 규칙이 너무 안 나오면 `MIN_SUPPORT`를 0.02 정도로 낮춰서 테스트
- 매 실행마다 전체 유저(또는 지정한 1명)에 대해 회귀 모델을 처음부터 재학습하는
  구조라, 레시피 수가 크게 늘면 실행 시간이 늘어날 수 있음 (증분 학습 없음)
- `difficulty`는 사용자가 레시피를 직접 등록(FR-24)할 때만 채워지는 필드라,
  지금처럼 공공데이터로 자동 수집된 레시피가 대부분이면 거의 비어있음. 값이
  1종류뿐이면 학습 특성에서 자동 제외되도록 방어 코드가 있음 (`train_novelty_model`
  참고). 사용자 등록 레시피가 늘어나면 자동으로 다시 유의미한 특성이 됨
- `is_essential`(필수 재료 구분) 컬럼은 DB에 아직 없어서, "필수 재료"를 임시로
  "조미료 아닌 전체 재료"로 간주하고 있음 (`fetch_recipe_essential_ingredient_ids`
  주석 참고). 나중에 컬럼이 생기면 그 쿼리만 고치면 됨
- 문서·발표 자료 표기: "AutoML(Apriori+PyCaret 회귀) 기반 추천"으로 표기하기로
  함 — 요구사항 정의서 FR-23 문구("협업 필터링 기반")와는 다르며, 팀 내 논의 후
  의도적으로 바꾼 것

---

## 2. 레시피 카테고리 자동 분류 (`classify_recipe_category.py`)

재료 원문 텍스트를 TF-IDF로 벡터화한 뒤 PyCaret 분류 모델로 레시피 카테고리
(반찬/국&찌개/후식/일품/밥/기타)를 예측하는 스크립트다.

> **필수 아님.** AutoML 설계 과제 검증용으로 만든 것이고, 실제 서비스 기능
> (레시피 등록 시 카테고리 자동 추천)에는 아직 연동 안 됨. 위 조합 추천과
> **같은 `venv_pycaret`**을 그대로 쓰면 된다 (별도 설치 불필요).

### 요구사항

- MySQL 서버 실행 중, `recipe`/`recipe_category` 테이블에 데이터가 있어야 함

### 실행 방법

```bash
python classify_recipe_category.py
```

`recipe_category_model.pkl`, `recipe_category_vectorizer.pkl`이 `ml/` 아래에
생성된다. 새 재료 원문으로 카테고리를 예측하려면 같은 파일의 `predict_category()`
함수를 사용.

### 언제 실행해야 하나

**수동 실행.** 학습 데이터(recipe + recipe_category)가 식약처 API 최초 수집 시
한 번만 채워지는 구조라, 레시피 데이터가 실제로 늘어나기 전까지는 다시 돌려도
결과가 거의 동일함.