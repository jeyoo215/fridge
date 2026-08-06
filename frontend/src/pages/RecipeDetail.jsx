import { useEffect, useState } from "react";
import { fetchRecipeDetail } from "../api/recipeApi";
import RecipeReviewSection from "../component/RecipeReviewSection"; // 1. 후기 섹션 컴포넌트 import 추가
import "./RecipeDetail.css";

// props로 recipeId, 뒤로가기용 onBack 콜백을 받음
// TODO: react-router 붙으면 useParams로 recipeId 받고, onBack은 navigate(-1)로 교체
export default function RecipeDetail({ recipeId, onBack }) {
  const [recipe, setRecipe] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchRecipeDetail(recipeId)
      .then(setRecipe)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [recipeId]);

  if (loading) return <p className="recipe-detail-status">불러오는 중...</p>;
  if (error) return <p className="recipe-detail-status">{error}</p>;
  if (!recipe) return null;

  return (
    <div className="recipe-detail-container">
      {onBack && (
        <button className="recipe-detail-back" onClick={onBack}>
          ← 목록으로
        </button>
      )}

      <h2 className="recipe-detail-title">{recipe.recipeName}</h2>
      <div className="recipe-detail-meta">
        <span>⏱ {recipe.cookingTimeMinutes}분</span>
        <span>· {recipe.difficulty}</span>
        <span>· {recipe.categoryName}</span>
      </div>

      <section className="recipe-detail-section">
        <h3>재료</h3>
        <ul className="recipe-detail-ingredient-list">
          {recipe.ingredients.map((item, idx) => (
            <li key={idx}>
              <span>{item.ingredientName}</span>
              <span className="recipe-detail-ingredient-amount">
                {item.quantity} {item.unit}
              </span>
            </li>
          ))}
        </ul>
      </section>

      <section className="recipe-detail-section">
        <h3>조리 순서</h3>
        <ol className="recipe-detail-step-list">
          {recipe.steps.map((step) => (
            <li key={step.stepOrder}>{step.description}</li>
          ))}
        </ol>
      </section>

      {/* 2. 조리 순서 아래(상세 화면 최하단)에 후기 섹션 추가 */}
      <RecipeReviewSection recipeId={recipe.recipeId} />
    </div>
  );
}