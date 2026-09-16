import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchRecommendedRecipes, fetchComboRecommendations } from "../api/recipeApi";
import "./RecipeCardGrid.css";

const PAGE_SIZE = 10;

export default function RecipeRecommendSection({ onEmptyRecommend }) {
  const navigate = useNavigate();
  const [onlyOwned, setOnlyOwned] = useState(false);
  const [page, setPage] = useState(0);
  const [pagedData, setPagedData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [comboRecipes, setComboRecipes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);

    if (onlyOwned) {
      fetchRecommendedRecipes(page, PAGE_SIZE)
        .then((result) => {
          setPagedData(result);
          if (page === 0 && result.totalElements === 0 && onEmptyRecommend) {
            onEmptyRecommend();
          }
        })
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false));
    } else {
      fetchComboRecommendations()
        .then(setComboRecipes)
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false));
    }
  }, [onlyOwned, page]);

  const handleToggleOnlyOwned = () => {
    setPage(0);
    setOnlyOwned((prev) => !prev);
  };

  const recipes = onlyOwned ? pagedData.content : comboRecipes;

  return (
    <div>
      <label className="recipe-filter-toggle">
        <input type="checkbox" checked={onlyOwned} onChange={handleToggleOnlyOwned} />
        있는 재료만 활용
      </label>

      <p className="combo-recommend-subtitle">
        {onlyOwned
          ? "지금 갖고 있는 재료로 바로 만들 수 있는 레시피예요"
          : "AI가 예측한 궁합 + 지금 냉장고 재료로 만들 수 있어요"}
      </p>

      {loading && <p className="recipe-status">불러오는 중...</p>}
      {error && <p className="recipe-status">{error}</p>}
      {!loading && !error && recipes.length === 0 && (
        <p className="recipe-status">
          {onlyOwned ? "재료를 더 등록해보세요!" : "아직 계산된 추천이 없어요."}
        </p>
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
              className="recipe-list-card"
              onClick={() => navigate(`/recipes/${recipe.recipeId}`)}
            >
              {recipe.imageUrl && (
                <div className="recipe-list-card-thumbnail">
                  <img src={recipe.imageUrl} alt={recipe.recipeName} />
                </div>
              )}
              <div className="recipe-list-card-info">
                {onlyOwned && (
                  <div className="recipe-list-card-badges">
                    {recipe.userCreated && <span className="recipe-user-badge">👑 유저 제작</span>}
                    {recipe.expiryPriorityScore > 0 && (
                      <span className="recipe-expiry-badge">🔥 유통기한 임박</span>
                    )}
                    {!recipe.hasAllTools && <span className="recipe-tool-badge">🔧 도구 부족</span>}
                  </div>
                )}
                <span className="recipe-list-card-name">{recipe.recipeName}</span>
                <span className="recipe-list-card-meta">
                  {onlyOwned ? "✅ 필요한 재료를 모두 갖고 있어요" : "✨ AI 추천"}
                  {metaParts.length > 0 ? ` · ${metaParts.join(" · ")}` : ""}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {onlyOwned && pagedData.totalPages > 1 && (
        <div className="recipe-list-pagination">
          <button
            className="recipe-list-pagination-arrow"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >
            ‹
          </button>
          <span className="recipe-list-pagination-info">
            {page + 1} / {pagedData.totalPages}
          </span>
          <button
            className="recipe-list-pagination-arrow"
            disabled={page + 1 >= pagedData.totalPages}
            onClick={() => setPage((p) => p + 1)}
          >
            ›
          </button>
        </div>
      )}
    </div>
  );
}