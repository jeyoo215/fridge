import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { fetchRecipeDetail } from "../api/recipeApi";
import RecipeReviewSection from "../component/RecipeReviewSection";
import "./RecipeDetail.css";

export default function RecipeDetail() {
  const { recipeId } = useParams();
  const navigate = useNavigate();
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
      <button className="recipe-detail-back" onClick={() => navigate(-1)}>
        ← 목록으로
      </button>

      <h2 className="recipe-detail-title">{recipe.recipeName}</h2>
      <div className="recipe-detail-meta">
        <span>⏱ {recipe.cookingTimeMinutes}분</span>
        <span>· {recipe.difficulty}</span>
        <span>· {recipe.categoryName}</span>
      </div>

      <Link to={`/recipes/${recipeId}/shopping-list`} className="recipe-detail-shopping-link">
        🛒 부족한 재료 장보기 리스트 보기
      </Link>

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

      <RecipeReviewSection recipeId={recipe.recipeId} />
    </div>
  );
}