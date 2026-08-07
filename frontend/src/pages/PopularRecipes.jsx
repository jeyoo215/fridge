import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchPopularRecipes } from "../api/socialApi";
import "./PopularRecipes.css";

export default function PopularRecipes() {
  const [recipes, setRecipes] = useState([]);
  const [sortBy, setSortBy] = useState("likes"); // "likes" | "reviews"
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    fetchPopularRecipes(sortBy)
      .then(setRecipes)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [sortBy]);

  return (
    <div className="popular-recipes-container">
      <h2 className="popular-recipes-title">인기 레시피</h2>

      <div className="popular-recipes-sort-toggle">
        <button
          className={sortBy === "likes" ? "active" : ""}
          onClick={() => setSortBy("likes")}
        >
          ❤️ 좋아요순
        </button>
        <button
          className={sortBy === "reviews" ? "active" : ""}
          onClick={() => setSortBy("reviews")}
        >
          💬 댓글순
        </button>
      </div>

      {loading && <p className="popular-recipes-status">불러오는 중...</p>}
      {error && <p className="popular-recipes-status">{error}</p>}
      {!loading && !error && recipes.length === 0 && (
        <p className="popular-recipes-status">아직 레시피가 없어요.</p>
      )}

      {recipes.map((recipe, index) => (
        <div
          key={recipe.recipeId}
          className="popular-recipe-card"
          onClick={() => navigate(`/recipes/${recipe.recipeId}`)}
        >
          <span className="popular-recipe-rank">{index + 1}</span>
          <div className="popular-recipe-info">
            <span className="popular-recipe-name">{recipe.recipeName}</span>
            <span className="popular-recipe-meta">
              ⏱ {recipe.cookingTimeMinutes}분 · {recipe.difficulty}
            </span>
          </div>
          <div className="popular-recipe-counts">
            <span>❤️ {recipe.likeCount}</span>
            <span>💬 {recipe.reviewCount}</span>
          </div>
        </div>
      ))}
    </div>
  );
}
