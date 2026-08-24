import { useEffect, useState } from "react";
import { fetchReviews, createReview } from "../api/recipeReviewApi";
import "./RecipeReviewSection.css";

// RecipeDetail.jsx 안에 <RecipeReviewSection recipeId={recipe.recipeId} /> 형태로 붙여서 사용
export default function RecipeReviewSection({ recipeId }) {
  const [data, setData] = useState(null);
  const [rating, setRating] = useState(5);
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const loadReviews = () => {
    fetchReviews(recipeId).then(setData).catch(console.error);
  };

  useEffect(() => {
    loadReviews();
  }, [recipeId]);

  const handleSubmit = async () => {
    if (submitting) return;
    setSubmitting(true);
    try {
      await createReview(recipeId, { rating, content });
      setContent("");
      setRating(5);
      loadReviews();
    } catch (err) {
      alert(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (!data) return null;

  return (
    <div className="review-section">
      <h3 className="review-section-title">
        후기 {data.reviewCount}개 · 평균 ★{data.averageRating.toFixed(1)}
      </h3>

      <div className="review-form">
        <select value={rating} onChange={(e) => setRating(Number(e.target.value))}>
          {[5, 4, 3, 2, 1].map((n) => (
            <option key={n} value={n}>{"★".repeat(n)}</option>
          ))}
        </select>
        <input
          type="text"
          placeholder="후기를 남겨보세요"
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <button onClick={handleSubmit} disabled={submitting}>등록</button>
      </div>

      <ul className="review-list">
        {data.reviews.map((review) => (
          <li key={review.reviewId} className="review-item">
            <span className="review-rating">{"★".repeat(review.rating)}</span>
            <span className="review-content">{review.content}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}