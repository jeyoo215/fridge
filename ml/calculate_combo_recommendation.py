import sys
import pandas as pd
import mysql.connector
from mlxtend.frequent_patterns import apriori, association_rules
from mlxtend.preprocessing import TransactionEncoder
from pycaret.regression import setup as reg_setup, compare_models, pull, predict_model, save_model

from db import load_db_config

MIN_SUPPORT = 0.05   # 데이터 적을 땐 0.02 정도로 낮춰서 테스트해도 됨
MIN_CONFIDENCE = 0.3
TOP_N_PER_USER = 15
INGREDIENT_MATCH_WEIGHT = 0.5  # 보유 재료 매칭 비율에 곱하는 가중치
MODEL_NAME = "recipe_novelty_model"


def fetch_df(conn, query):
    return pd.read_sql(query, conn)


# ── 1. Apriori: ML 학습용 "정답 라벨" 생성 (내부 데이터: recipe_ingredient) ──

def build_recipe_baskets(conn):
    df = fetch_df(conn, """
        SELECT ri.recipe_id, i.ingredient_name
        FROM recipe_ingredient ri
        JOIN ingredient i ON i.ingredient_id = ri.ingredient_id
        WHERE i.is_seasoning = false
    """)
    return df.groupby("recipe_id")["ingredient_name"].apply(list)


def mine_pairwise_rules(baskets):
    te = TransactionEncoder()
    te_array = te.fit(baskets.tolist()).transform(baskets.tolist())
    onehot = pd.DataFrame(te_array, columns=te.columns_)

    frequent_itemsets = apriori(onehot, min_support=MIN_SUPPORT, use_colnames=True)
    if frequent_itemsets.empty:
        return pd.DataFrame(columns=["ingredient_a", "ingredient_b", "lift"])

    rules = association_rules(frequent_itemsets, metric="confidence", min_threshold=MIN_CONFIDENCE)
    pairwise = rules[
        (rules["antecedents"].apply(len) == 1) & (rules["consequents"].apply(len) == 1)
    ].copy()
    pairwise["ingredient_a"] = pairwise["antecedents"].apply(lambda x: next(iter(x)))
    pairwise["ingredient_b"] = pairwise["consequents"].apply(lambda x: next(iter(x)))
    return pairwise[["ingredient_a", "ingredient_b", "lift"]]


def build_rule_lookup(pairwise_rules):
    lookup = {}
    for _, row in pairwise_rules.iterrows():
        key = frozenset([row["ingredient_a"], row["ingredient_b"]])
        lookup[key] = lookup.get(key, 0) + row["lift"]
    return lookup


def compute_recipe_novelty_labels(baskets, rule_lookup):
    """레시피별 '궁합 지수' = 그 레시피 안 재료쌍들의 lift 합. ML 학습 라벨로 사용."""
    labels = {}
    for recipe_id, ingredients in baskets.items():
        score = 0.0
        for i in range(len(ingredients)):
            for j in range(i + 1, len(ingredients)):
                pair = frozenset([ingredients[i], ingredients[j]])
                score += rule_lookup.get(pair, 0)
        labels[recipe_id] = score
    return labels


# ── 2. PyCaret 회귀: 추천 점수를 만들어내는 핵심 엔진 (내부 데이터: recipe) ──

def fetch_recipe_features(conn):
    return fetch_df(conn, """
        SELECT r.recipe_id, r.calorie, r.carbohydrate, r.protein, r.fat, r.sodium,
               r.cooking_time_minutes, r.difficulty, c.category_name,
               (SELECT COUNT(*) FROM recipe_ingredient ri
                WHERE ri.recipe_id = r.recipe_id) AS ingredient_count
        FROM recipe r
        JOIN recipe_category c ON r.category_id = c.category_id
    """)


def train_novelty_model(recipe_features, novelty_labels):
    """Apriori 궁합 지수를 라벨 삼아, 레시피 특성(영양성분/난이도/카테고리)만으로
    그 지수를 예측하는 회귀 모델을 AutoML로 학습. Apriori 규칙이 안 걸리는
    레시피(라벨=0)에도 일반화된 예측치를 줘서, 순수 Apriori보다 추천 커버리지가 넓어짐."""
    df = recipe_features.copy()
    df["novelty_score"] = df["recipe_id"].map(novelty_labels).fillna(0)
    train_df = df.drop(columns=["recipe_id"])

    reg_setup(
        data=train_df,
        target="novelty_score",
        categorical_features=["difficulty", "category_name"],
        session_id=42,
        train_size=0.8,
        verbose=False,
    )

    best_model = compare_models(sort="MAE")
    leaderboard = pull()
    print("[모델 비교 결과 - compare_models()]")
    print(leaderboard.to_string())

    save_model(best_model, MODEL_NAME)
    return best_model


def predict_novelty_scores(model, recipe_features):
    input_df = recipe_features.drop(columns=["recipe_id"])
    predictions = predict_model(model, data=input_df)
    label_col = "prediction_label" if "prediction_label" in predictions.columns else "Label"
    return dict(zip(recipe_features["recipe_id"], predictions[label_col]))


# ── 3. 사용자별 개인화: 안 먹어본 조합 + 보유 재료 매칭 (내부 데이터) ────

def fetch_all_reviews(conn):
    return fetch_df(conn, """
        SELECT rr.user_id, ri.recipe_id, i.ingredient_name
        FROM recipe_review rr
        JOIN recipe_ingredient ri ON ri.recipe_id = rr.recipe_id
        JOIN ingredient i ON i.ingredient_id = ri.ingredient_id
        WHERE i.is_seasoning = false
    """)


def build_user_tried_pairs_and_reviewed(all_reviews):
    tried_pairs_by_user = {}
    reviewed_by_user = {}
    for user_id, user_df in all_reviews.groupby("user_id"):
        reviewed_by_user[user_id] = set(user_df["recipe_id"].unique())
        pairs = set()
        for _, group in user_df.groupby("recipe_id"):
            names = group["ingredient_name"].tolist()
            for i in range(len(names)):
                for j in range(i + 1, len(names)):
                    pairs.add(frozenset([names[i], names[j]]))
        tried_pairs_by_user[user_id] = pairs
    return tried_pairs_by_user, reviewed_by_user


def fetch_user_owned_ingredients(conn, user_id):
    df = fetch_df(conn, f"""
        SELECT ingredient_id FROM user_ingredient
        WHERE user_id = {user_id} AND status = '보유중'
    """)
    return set(df["ingredient_id"].tolist())


def fetch_recipe_ingredient_ids(conn):
    df = fetch_df(conn, """
        SELECT ri.recipe_id, ri.ingredient_id
        FROM recipe_ingredient ri
        JOIN ingredient i ON i.ingredient_id = ri.ingredient_id
        WHERE i.is_seasoning = false
    """)
    return df.groupby("recipe_id")["ingredient_id"].apply(list).to_dict()


def compute_tried_ratio(ingredients, tried_pairs):
    """이 레시피 재료쌍 중 사용자가 이미 먹어본 조합 비율 (0~1) - ML 예측치 할인용"""
    total, tried = 0, 0
    for i in range(len(ingredients)):
        for j in range(i + 1, len(ingredients)):
            total += 1
            if frozenset([ingredients[i], ingredients[j]]) in tried_pairs:
                tried += 1
    return tried / total if total else 0.0


def compute_match_ratio(recipe_ingredient_ids, owned_ingredient_ids):
    if not recipe_ingredient_ids:
        return 0.0
    matched = len(set(recipe_ingredient_ids) & owned_ingredient_ids)
    return matched / len(recipe_ingredient_ids)


def save_scores(conn, user_id, scores):
    cursor = conn.cursor()
    cursor.execute("DELETE FROM combination_recommendation WHERE user_id = %s", (user_id,))
    for recipe_id, score in scores:
        cursor.execute(
            "INSERT INTO combination_recommendation (user_id, recipe_id, combo_score, generated_at) "
            "VALUES (%s, %s, %s, NOW())",
            (user_id, recipe_id, float(score)),
        )
    conn.commit()
    cursor.close()


def main():
    conn = mysql.connector.connect(**load_db_config())

    # 1. Apriori로 레시피별 궁합 지수(ML 학습 라벨) 생성
    baskets = build_recipe_baskets(conn)
    pairwise_rules = mine_pairwise_rules(baskets)
    rule_lookup = build_rule_lookup(pairwise_rules)
    novelty_labels = compute_recipe_novelty_labels(baskets, rule_lookup)
    print(f"연관 규칙 {len(pairwise_rules)}개 발견 (ML 라벨 생성용)")

    # 2. PyCaret 회귀 모델 학습 - 진짜 AutoML (여러 알고리즘 자동 비교 후 최적 선택)
    recipe_features = fetch_recipe_features(conn)
    model = train_novelty_model(recipe_features, novelty_labels)
    predicted_novelty = predict_novelty_scores(model, recipe_features)

    recipe_ingredient_ids = fetch_recipe_ingredient_ids(conn)

    # 3. 사용자별 리뷰 기록(이미 먹어본 조합) + 보유 재료
    all_reviews = fetch_all_reviews(conn)
    tried_pairs_by_user, reviewed_by_user = build_user_tried_pairs_and_reviewed(all_reviews)

    if len(sys.argv) > 1:
        user_ids = [int(sys.argv[1])]
    else:
        user_ids = fetch_df(conn, "SELECT DISTINCT user_id FROM user")["user_id"].tolist()

    # 4. 사용자별 최종 추천 점수 = ML 예측치 × (1-이미 먹어본 비율) + 재료 매칭 가산점
    for user_id in user_ids:
        tried_pairs = tried_pairs_by_user.get(user_id, set())
        reviewed_ids = reviewed_by_user.get(user_id, set())
        owned_ingredients = fetch_user_owned_ingredients(conn, user_id)

        scores = []
        for recipe_id, ingredients in baskets.items():
            if recipe_id in reviewed_ids:
                continue

            base_score = predicted_novelty.get(recipe_id, 0.0)
            tried_ratio = compute_tried_ratio(ingredients, tried_pairs)
            match_ratio = compute_match_ratio(recipe_ingredient_ids.get(recipe_id, []), owned_ingredients)

            final_score = base_score * (1 - tried_ratio) + INGREDIENT_MATCH_WEIGHT * match_ratio
            if final_score > 0:
                scores.append((recipe_id, final_score))

        scores.sort(key=lambda x: x[1], reverse=True)
        top_scores = scores[:TOP_N_PER_USER]

        save_scores(conn, user_id, top_scores)
        print(f"user_id={user_id}: 추천 {len(top_scores)}개 저장")

    conn.close()


if __name__ == "__main__":
    main()