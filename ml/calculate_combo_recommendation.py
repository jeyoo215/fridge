import pandas as pd
import mysql.connector
from mlxtend.frequent_patterns import apriori, association_rules
from mlxtend.preprocessing import TransactionEncoder
import sys

from db import load_db_config

MIN_SUPPORT = 0.05   # 데이터 적을 땐 0.02 정도로 낮춰서 테스트해도 됨
MIN_CONFIDENCE = 0.3
TOP_N_PER_USER = 5

# Apriori 연관 규칙 마이닝

def fetch_df(conn, query):
    return pd.read_sql(query, conn)


# 모든 레시피의 재료 구성
def build_recipe_baskets(conn):
    # 조미료는 거의 모든 레시피에 들어가서 규칙을 왜곡시키므로 제외
    df = fetch_df(conn, """
        SELECT ri.recipe_id, i.ingredient_name
        FROM recipe_ingredient ri
        JOIN ingredient i ON i.ingredient_id = ri.ingredient_id
        WHERE i.is_seasoning = false
    """)
    return df.groupby("recipe_id")["ingredient_name"].apply(list)


# 그 레시피 장바구니들 기반 Apriori 연관 규칙
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


# reviewed_ids → 사용자의 리뷰 기록
def fetch_user_tried_pairs(conn, user_id):
    # 사용자가 리뷰를 남긴 레시피 = 이미 만들어본 레시피로 간주, 그 안의 재료쌍은 "이미 먹어본 조합"
    df = fetch_df(conn, f"""
        SELECT ri.recipe_id, i.ingredient_name
        FROM recipe_review rr
        JOIN recipe_ingredient ri ON ri.recipe_id = rr.recipe_id
        JOIN ingredient i ON i.ingredient_id = ri.ingredient_id
        WHERE rr.user_id = {user_id} AND i.is_seasoning = false
    """)
    tried_pairs = set()
    for _, group in df.groupby("recipe_id"):
        names = group["ingredient_name"].tolist()
        for i in range(len(names)):
            for j in range(i + 1, len(names)):
                tried_pairs.add(frozenset([names[i], names[j]]))
    return tried_pairs


def build_rule_lookup(pairwise_rules):
    lookup = {}
    for _, row in pairwise_rules.iterrows():
        key = frozenset([row["ingredient_a"], row["ingredient_b"]])
        lookup[key] = lookup.get(key, 0) + row["lift"]
    return lookup


def compute_recipe_scores(reviewed_ids, baskets, rule_lookup, tried_pairs):
    scores = []
    for recipe_id, ingredients in baskets.items():
        if recipe_id in reviewed_ids:
            continue
        score = 0.0
        for i in range(len(ingredients)):
            for j in range(i + 1, len(ingredients)):
                pair = frozenset([ingredients[i], ingredients[j]])
                if pair in tried_pairs:
                    continue
                score += rule_lookup.get(pair, 0)
        if score > 0:
            scores.append((recipe_id, score))

    scores.sort(key=lambda x: x[1], reverse=True)
    return scores[:TOP_N_PER_USER]


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




def main():
    conn = mysql.connector.connect(**load_db_config())

    baskets = build_recipe_baskets(conn)
    pairwise_rules = mine_pairwise_rules(baskets)
    rule_lookup = build_rule_lookup(pairwise_rules)
    print(f"연관 규칙 {len(pairwise_rules)}개 발견")

    all_reviews = fetch_all_reviews(conn)
    tried_pairs_by_user, reviewed_by_user = build_user_tried_pairs_and_reviewed(all_reviews)

    if len(sys.argv) > 1:
        user_ids = [int(sys.argv[1])]
    else:
        user_ids = fetch_df(conn, "SELECT DISTINCT user_id FROM user")["user_id"].tolist()

    for user_id in user_ids:
        tried_pairs = tried_pairs_by_user.get(user_id, set())
        reviewed_ids = reviewed_by_user.get(user_id, set())
        scores = compute_recipe_scores(reviewed_ids, baskets, rule_lookup, tried_pairs)
        save_scores(conn, user_id, scores)
        print(f"user_id={user_id}: 추천 {len(scores)}개 저장")

    conn.close()


if __name__ == "__main__":
    main()