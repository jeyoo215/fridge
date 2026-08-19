import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { fetchRecipeDetail } from "../api/recipeApi";
import { fetchLikeStatus, toggleLike, fetchScrapStatus, toggleScrap } from "../api/socialApi";
import { toMediaSrc } from "../api/communityApi";
import RecipeReviewSection from "../component/RecipeReviewSection";
import "./RecipeDetail.css";

const TEMP_USER_ID = 1; // TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체

export default function RecipeDetail() {
  const { recipeId } = useParams();
  const navigate = useNavigate();
  const [recipe, setRecipe] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 좋아요/스크랩 상태 (나경님 파트: social 도메인)
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [scraped, setScraped] = useState(false);
  const [scrapCount, setScrapCount] = useState(0);

  useEffect(() => {
    fetchRecipeDetail(recipeId)
      .then(setRecipe)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));

    fetchLikeStatus(recipeId, TEMP_USER_ID)
      .then((res) => {
        setLiked(res.active);
        setLikeCount(res.count);
      })
      .catch(() => {});

    fetchScrapStatus(recipeId, TEMP_USER_ID)
      .then((res) => {
        setScraped(res.active);
        setScrapCount(res.count);
      })
      .catch(() => {});
  }, [recipeId]);

  const handleToggleLike = async () => {
    try {
      const res = await toggleLike(recipeId, TEMP_USER_ID);
      setLiked(res.active);
      setLikeCount(res.count);
    } catch {
      // 실패해도 화면은 그대로 두고 조용히 무시 (좋아요는 핵심 흐름이 아니라서)
    }
  };

  const handleToggleScrap = async () => {
    try {
      const res = await toggleScrap(recipeId, TEMP_USER_ID);
      setScraped(res.active);
      setScrapCount(res.count);
    } catch {
      // 실패해도 조용히 무시
    }
  };

  if (loading) return <p className="recipe-detail-status">불러오는 중...</p>;
  if (error) return <p className="recipe-detail-status">{error}</p>;
  if (!recipe) return null;

  return (
    <div className="recipe-detail-container">
      <button className="recipe-detail-back" onClick={() => navigate(-1)}>
        ← 목록으로
      </button>

      <h2 className="recipe-detail-title">
        {recipe.recipeName}
        {recipe.userCreated && <span className="recipe-user-badge">👑 유저 제작 레시피</span>}
      </h2>
      <div className="recipe-detail-meta">
        <span>⏱ {recipe.cookingTimeMinutes}분</span>
        <span>· {recipe.difficulty}</span>
        <span>· {recipe.categoryName}</span>
      </div>

      <div className="recipe-social-actions">
        <button
          className={`recipe-social-button ${liked ? "active" : ""}`}
          onClick={handleToggleLike}
        >
          {liked ? "❤️" : "🤍"} 좋아요 {likeCount}
        </button>
        <button
          className={`recipe-social-button ${scraped ? "active" : ""}`}
          onClick={handleToggleScrap}
        >
          {scraped ? "🔖" : "📑"} 스크랩 {scrapCount}
        </button>
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
            <li key={step.stepOrder}>
              {step.mediaUrl && step.mediaType === "VIDEO" && (
                <video className="recipe-detail-step-media" src={toMediaSrc(step.mediaUrl)} controls />
              )}
              {step.mediaUrl && step.mediaType === "IMAGE" && (
                <img className="recipe-detail-step-media" src={toMediaSrc(step.mediaUrl)} alt={`${step.stepOrder}단계`} />
              )}
              {step.description}
            </li>
          ))}
        </ol>
      </section>

      <RecipeReviewSection recipeId={recipe.recipeId} />
    </div>
  );
}
