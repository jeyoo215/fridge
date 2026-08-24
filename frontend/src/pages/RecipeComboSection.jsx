import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchComboRecommendations } from "../api/recipeApi";
import "./RecipeCardGrid.css";


export default function RecipeComboSection() {
  const navigate = useNavigate();
  const [recipes, setRecipes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchComboRecommendations()
      .then(setRecipes)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="recipe-status">불러오는 중...</p>;
  if (error) return <p className="recipe-status">{error}</p>;

  return (
    <div>
      <p className="combo-recommend-subtitle">아직 안 써본 재료 조합으로 만들 수 있어요</p>

      {recipes.length === 0 && (
        <p className="recipe-status">아직 계산된 조합 추천이 없어요.</p>
      )}

      <div className="recipe-list-grid">
        {recipes.map((recipe) => {
          const metaParts = [
            recipe.cookingTimeMinutes > 0 ? `⏱ ${recipe.cookingTimeMinutes}분` : null,
            recipe.difficulty || null,
          ].filter(Boolean);

          return (
            <div
              key={recipe.recipeId}
              className="recipe-list-card combo-recipe-card"
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
                  ✨ 새로운 조합 추천{metaParts.length > 0 ? ` · ${metaParts.join(" · ")}` : ""}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}