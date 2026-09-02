import { getAccessToken } from "./authApi";

const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

function authHeaders() {
  return { Authorization: `Bearer ${getAccessToken()}` };
}

// 보유 재료 기반 레시피 추천 목록 조회
export async function fetchRecommendedRecipes(page = 0, size = 10) {
  const response = await fetch(`${BASE_URL}/recipes/recommend?page=${page}&size=${size}`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("추천 레시피를 불러오지 못했습니다.");
  }
  return response.json();
}

// 레시피 상세 조회 (FR-24) — 공용 조회, 토큰 불필요
export async function fetchRecipeDetail(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}`);

  if (!response.ok) {
    throw new Error("레시피 상세 정보를 불러오지 못했습니다.");
  }

  return response.json();
}

// AutoML 추천 레시피
export async function fetchComboRecommendations() {
  const response = await fetch(`${BASE_URL}/recipes/combo-recommend`, {
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("의외의 조합 추천을 불러오지 못했습니다.");
  return response.json();
}

// 레시피 카테고리 전체 목록 (커뮤니티 글쓰기 화면 드롭다운용) — 공용 조회, 토큰 불필요
export async function fetchRecipeCategories() {
  const response = await fetch(`${BASE_URL}/recipes/categories`);
  if (!response.ok) throw new Error("카테고리 목록을 불러오지 못했습니다.");
  return response.json();
}

// 레시피 목록/검색 — 공용 조회, 토큰 불필요
export async function fetchRecipeList({ keyword = "", ingredientIds = [], page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({ page, size });
  if (keyword) params.set("keyword", keyword);
  if (ingredientIds.length > 0) params.set("ingredientIds", ingredientIds.join(","));

  const response = await fetch(`${BASE_URL}/recipes?${params.toString()}`);
  if (!response.ok) throw new Error("레시피 목록을 불러오지 못했습니다.");
  return response.json();
}
