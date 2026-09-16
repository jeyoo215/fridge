"""
레시피 재료 구성(텍스트) → TF-IDF 특징 변환 → PyCaret 분류로 카테고리 예측
(반찬 / 국&찌개 / 후식 / 일품 / 밥 / 기타)

AutoML 과제용 설계 검증 스크립트.
"""
import re
import joblib
import pandas as pd
import mysql.connector
from sklearn.feature_extraction.text import TfidfVectorizer
from pycaret.classification import setup, compare_models, pull, save_model, predict_model, load_model

from db import load_db_config

MAX_FEATURES = 300


def fetch_recipes(conn):
    query = """
        SELECT r.recipe_id, r.raw_ingredients, c.category_name
        FROM recipe r
        JOIN recipe_category c ON r.category_id = c.category_id
        WHERE r.raw_ingredients IS NOT NULL AND r.raw_ingredients != ''
    """
    return pd.read_sql(query, conn)


# 재료 원문("돼지고기 200g, 김치 1/4포기, 대파 1대...")에서
# 수량/단위/괄호 설명을 제거해서 "재료명"만 남김 - 안 그러면 TF-IDF가 숫자/단위에 낚임
UNIT_PATTERN = re.compile(
    r"\d+(\.\d+)?|g|kg|ml|l|개|큰술|작은술|컵|모|포기|대|알|장|줌|꼬집|공기|조금|약간"
)


def clean_ingredients(raw: str) -> str:
    text = re.sub(r"\(.*?\)", " ", raw)     # 괄호 설명 제거
    text = UNIT_PATTERN.sub(" ", text)       # 수량/단위 제거
    text = re.sub(r"[,/·|]", " ", text)      # 구분자 → 공백
    text = re.sub(r"\s+", " ", text).strip()
    return text


def build_feature_frame(df: pd.DataFrame):
    df["cleaned"] = df["raw_ingredients"].apply(clean_ingredients)

    vectorizer = TfidfVectorizer(max_features=MAX_FEATURES, min_df=2)
    tfidf_matrix = vectorizer.fit_transform(df["cleaned"])

    feature_df = pd.DataFrame(
        tfidf_matrix.toarray(),
        columns=[f"tfidf_{i}" for i in range(tfidf_matrix.shape[1])],
    )
    feature_df["category"] = df["category_name"].values
    return feature_df, vectorizer


def main():
    conn = mysql.connector.connect(**load_db_config())
    df = fetch_recipes(conn)
    conn.close()

    print(f"레시피 {len(df)}건 로드 완료")
    print(df["category_name"].value_counts())

    feature_df, vectorizer = build_feature_frame(df)

    # PyCaret 분류 파이프라인: setup → compare_models → 평가
    setup(
        data=feature_df,
        target="category",
        session_id=42,
        train_size=0.8,
        normalize=False,   # TF-IDF는 이미 0~1 스케일이라 추가 정규화 불필요
        verbose=False,
    )

    best_model = compare_models(sort="F1")
    leaderboard = pull()
    print(leaderboard)

    save_model(best_model, "recipe_category_model")
    joblib.dump(vectorizer, "recipe_category_vectorizer.pkl")
    print("모델/벡터라이저 저장 완료")


def predict_category(raw_ingredients: str):
    """새 레시피 재료 원문 → 카테고리 예측 (서비스 연동 시 사용)"""
    vectorizer = joblib.load("recipe_category_vectorizer.pkl")
    model = load_model("recipe_category_model")

    cleaned = clean_ingredients(raw_ingredients)
    tfidf_vec = vectorizer.transform([cleaned])
    feature_df = pd.DataFrame(
        tfidf_vec.toarray(),
        columns=[f"tfidf_{i}" for i in range(tfidf_vec.shape[1])],
    )

    result = predict_model(model, data=feature_df)
    return result[["prediction_label", "prediction_score"]].iloc[0]


if __name__ == "__main__":
    main()