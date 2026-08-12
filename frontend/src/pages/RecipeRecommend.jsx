import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchRecommendedRecipes, fetchComboRecommendations } from "../api/recipeApi";
import "./RecipeRecommend.css";

// TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체하기
const TEMP_USER_ID = 1;

export default function RecipeRecommend() {
  const [recipes, setRecipes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [comboRecipes, setComboRecipes] = useState([]);
  const [comboLoading, setComboLoading] = useState(true);

  const navigate = useNavigate();

  useEffect(() => {
    fetchRecommendedRecipes(TEMP_USER_ID)
      .then(setRecipes)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));

    // 의외의 조합 추천은 실패해도 메인 추천 흐름에 영향 없게 별도로 처리 (FR-23)
    fetchComboRecommendations(TEMP_USER_ID)
      .then(setComboRecipes)
      .catch(() => setComboRecipes([]))
      .finally(() => setComboLoading(false));
  }, []);

  if (loading) return <p className="recipe-status">불러오는 중...</p>;
  if (error) return <p className="recipe-status">{error}</p>;

  return (
    <div className="recipe-recommend-container">
      <h2 className="recipe-recommend-title">지금 만들 수 있는 레시피</h2>

      {recipes.length === 0 && (
        <p className="recipe-status">추천할 레시피가 없어요. 재료를 더 등록해보세요!</p>
      )}

      {recipes.map((recipe) => (
        <div
          key={recipe.recipeId}
          className="recipe-card"
          onClick={() => navigate(`/recipes/${recipe.recipeId}`)}
          style={{ cursor: "pointer" }}
        >
          <div className="recipe-card-info">
            <div className="recipe-name-row">
              <span className="recipe-name">{recipe.recipeName}</span>
              {recipe.expiryPriorityScore > 0 && (
                <span className="recipe-expiry-badge">🔥 유통기한 임박 재료 활용</span>
              )}
              {!recipe.hasAllTools && (
                <span className="recipe-tool-badge">🔧 도구 부족</span>
              )}
            </div>
            <span className="recipe-match">
              보유 재료 {recipe.matchCount}/{recipe.totalIngredientCount}개 일치
            </span>
          </div>
          <div className="recipe-card-meta">
            <span>⏱ {recipe.cookingTimeMinutes}분</span>
            <span>· {recipe.difficulty}</span>
          </div>
        </div>
      ))}

      {!comboLoading && comboRecipes.length > 0 && (
        <section className="combo-recommend-section">
          <h3 className="combo-recommend-title">🎲 이런 조합은 어때요?</h3>
          <p className="combo-recommend-subtitle">아직 안 써본 재료 조합으로 만들 수 있어요</p>

          {comboRecipes.map((recipe) => (
            <div
              key={recipe.recipeId}
              className="recipe-card combo-recipe-card"
              onClick={() => navigate(`/recipes/${recipe.recipeId}`)}
              style={{ cursor: "pointer" }}
            >
              <div className="recipe-card-info">
                <span className="recipe-name">{recipe.recipeName}</span>
                <span className="recipe-match">✨ 새로운 조합 추천</span>
              </div>
            </div>
          ))}
        </section>
      )}
    </div>
  );
}