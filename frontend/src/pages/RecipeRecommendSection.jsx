import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchComboRecommendations } from "../api/recipeApi";
import "./RecipeCardGrid.css";

export default function RecipeRecommendSection({ onEmptyRecommend }) {
  const navigate = useNavigate();
  const [onlyOwned, setOnlyOwned] = useState(false);
  const [recipes, setRecipes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    fetchComboRecommendations(onlyOwned)
      .then((result) => {
        setRecipes(result);
        if (result.length === 0 && onEmptyRecommend) onEmptyRecommend();
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [onlyOwned]);

  if (loading) return <p className="recipe-status">불러오는 중...</p>;
  if (error) return <p className="recipe-status">{error}</p>;

  return (
    <div>
      <label className="recipe-filter-toggle">
        <input type="checkbox" checked={onlyOwned} onChange={() => setOnlyOwned((v) => !v)} />
        있는 재료만 활용
      </label>

      <p className="combo-recommend-subtitle">
        {onlyOwned
          ? "지금 갖고 있는 재료로 바로 만들 수 있는 레시피예요"
          : "AI가 예측한 궁합 + 지금 냉장고 재료로 만들 수 있어요"}
      </p>

      {recipes.length === 0 && <p className="recipe-status">아직 계산된 추천이 없어요.</p>}

      <div className="recipe-list-grid">
        {recipes.map((recipe) => {
          const metaParts = [
            recipe.cookingTimeMinutes > 0 ? `⏱ ${recipe.cookingTimeMinutes}분` : null,
            recipe.difficulty || null,
          ].filter(Boolean);

          return (
            <div
              key={recipe.recipeId}
              className="recipe-list-card"
              onClick={() => navigate(`/recipes/${recipe.recipeId}`)}
            >
              {recipe.imageUrl && (
                <div className="recipe-list-card-thumbnail">
                  <img src={recipe.imageUrl} alt={recipe.recipeName} />
                </div>
              )}
              <div className="recipe-list-card-info">
                <span className="recipe-list-card-name">{recipe.recipeName}</span>
                <span className="recipe-list-card-meta">
                  ✨ AI 추천{metaParts.length > 0 ? ` · ${metaParts.join(" · ")}` : ""}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}