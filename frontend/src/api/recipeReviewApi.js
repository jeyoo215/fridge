import { getAccessToken } from "./authApi";
import { BASE_URL } from "./config";

// ?�기 목록 + ?�균 ?�점 조회 ??공용 조회, ?�큰 불필??
export async function fetchReviews(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/reviews`);
  if (!response.ok) throw new Error("?�기�?불러?��? 못했?�니??");
  return response.json();
}

// ?�기 ?�록
export async function createReview(recipeId, { rating, content }) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/reviews`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getAccessToken()}`,
    },
    body: JSON.stringify({ rating, content }),
  });
  if (!response.ok) throw new Error("?�기 ?�록???�패?�습?�다.");
  return response.json();
}
