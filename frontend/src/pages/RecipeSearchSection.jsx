import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchRecipeList } from "../api/recipeApi";
import { searchIngredients } from "../api/ingredientApi";
import "./RecipeSearchSection.css";

const PAGE_SIZE = 20;

export default function RecipeSearchSection({ notice }) {
  const navigate = useNavigate();

  const [keywordInput, setKeywordInput] = useState("");
  const [keyword, setKeyword] = useState("");

  const [ingredientKeyword, setIngredientKeyword] = useState("");
  const [ingredientResults, setIngredientResults] = useState([]);
  const [selectedIngredients, setSelectedIngredients] = useState([]); // [{ingredientId, ingredientName}]

  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 재료 검색 자동완성
  useEffect(() => {
    if (!ingredientKeyword) {
      setIngredientResults([]);
      return;
    }
    const timer = setTimeout(() => {
      searchIngredients(ingredientKeyword).then(setIngredientResults).catch(() => {});
    }, 300);
    return () => clearTimeout(timer);
  }, [ingredientKeyword]);

  // 검색조건 바뀌면 목록 재조회 (페이지는 0으로 리셋)
  useEffect(() => {
    setLoading(true);
    fetchRecipeList({
      keyword,
      ingredientIds: selectedIngredients.map((i) => i.ingredientId),
      page,
      size: PAGE_SIZE,
    })
      .then(setData)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [keyword, selectedIngredients, page]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim());
  };

  const handleAddIngredient = (ingredient) => {
    if (selectedIngredients.some((i) => i.ingredientId === ingredient.ingredientId)) return;
    setSelectedIngredients([...selectedIngredients, ingredient]);
    setIngredientKeyword("");
    setIngredientResults([]);
    setPage(0);
  };

  const handleRemoveIngredient = (ingredientId) => {
    setSelectedIngredients(selectedIngredients.filter((i) => i.ingredientId !== ingredientId));
    setPage(0);
  };

  return (
    <div className="recipe-search-section">
      {notice && <p className="recipe-search-notice">{notice}</p>}

      <form className="recipe-list-search-form" onSubmit={handleSearchSubmit}>
        <input
          type="text"
          placeholder="레시피 이름으로 검색"
          value={keywordInput}
          onChange={(e) => setKeywordInput(e.target.value)}
        />
        <button type="submit">검색</button>
      </form>

      <div className="recipe-list-ingredient-filter">
        <input
          type="text"
          placeholder="재료로 필터 (예: 양파)"
          value={ingredientKeyword}
          onChange={(e) => setIngredientKeyword(e.target.value)}
        />
        {ingredientResults.length > 0 && (
          <ul className="recipe-list-ingredient-results">
            {ingredientResults.map((ingredient) => (
              <li key={ingredient.ingredientId} onClick={() => handleAddIngredient(ingredient)}>
                {ingredient.ingredientName}
              </li>
            ))}
          </ul>
        )}
        {selectedIngredients.length > 0 && (
          <ul className="recipe-list-ingredient-chips">
            {selectedIngredients.map((ingredient) => (
              <li key={ingredient.ingredientId} className="recipe-list-ingredient-chip">
                {ingredient.ingredientName}
                <button onClick={() => handleRemoveIngredient(ingredient.ingredientId)}>✕</button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {loading && <p className="recipe-status">불러오는 중...</p>}
      {error && <p className="recipe-status">{error}</p>}
      {!loading && !error && data.content.length === 0 && (
        <p className="recipe-status">조건에 맞는 레시피가 없어요.</p>
      )}

      <div className="recipe-list-grid">
        {data.content.map((recipe) => {
          const metaParts = [
            recipe.categoryName || null,
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
                <span className="recipe-list-card-meta">{metaParts.join(" · ")}</span>
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