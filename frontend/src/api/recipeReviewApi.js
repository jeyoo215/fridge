import { getAccessToken } from "./authApi";

const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

// 후기 목록 + 평균 평점 조회 — 공용 조회, 토큰 불필요
export async function fetchReviews(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/reviews`);
  if (!response.ok) throw new Error("후기를 불러오지 못했습니다.");
  return response.json();
}

// 후기 등록
export async function createReview(recipeId, { rating, content }) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/reviews`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getAccessToken()}`,
    },
    body: JSON.stringify({ rating, content }),
  });
  if (!response.ok) throw new Error("후기 등록에 실패했습니다.");
  return response.json();
}
