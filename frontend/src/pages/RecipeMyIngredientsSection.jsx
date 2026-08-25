import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchRecommendedRecipes } from "../api/recipeApi";
import { getCurrentUserId } from "../api/authApi";
import "./RecipeCardGrid.css";

const TEMP_USER_ID = getCurrentUserId() ?? 1; // 로그인 안 했으면 1(seed 계정)로 폴백
const PAGE_SIZE = 10;

export default function RecipeMyIngredientsSection() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    fetchRecommendedRecipes(TEMP_USER_ID, page, PAGE_SIZE)
      .then(setData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [page]);

  if (loading) return <p className="recipe-status">불러오는 중...</p>;
  if (error) return <p className="recipe-status">{error}</p>;

  return (
    <div>
      {data.content.length === 0 && <p className="recipe-status">재료를 더 등록해보세요!</p>}

      <div className="recipe-list-grid">
        {data.content.map((recipe) => {
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
                <div className="recipe-list-card-badges">
                  {recipe.userCreated && <span className="recipe-user-badge">👑 유저 제작</span>}
                  {recipe.expiryPriorityScore > 0 && (
                    <span className="recipe-expiry-badge">🔥 유통기한 임박</span>
                  )}
                  {!recipe.hasAllTools && <span className="recipe-tool-badge">🔧 도구 부족</span>}
                </div>
                <span className="recipe-list-card-name">{recipe.recipeName}</span>
                <span className="recipe-list-card-meta">
                  ✅ 필요한 재료를 모두 갖고 있어요
                  {metaParts.length > 0 ? ` · ${metaParts.join(" · ")}` : ""}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {data.totalPages > 1 && (
        <div className="recipe-list-pagination">
          <button
            className="recipe-list-pagination-arrow"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >
            ‹
          </button>
          <span className="recipe-list-pagination-info">
            {page + 1} / {data.totalPages}
          </span>
          <button
            className="recipe-list-pagination-arrow"
            disabled={page + 1 >= data.totalPages}
            onClick={() => setPage((p) => p + 1)}
          >
            ›
          </button>
        </div>
      )}
    </div>
  );
}