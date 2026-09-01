import { getAccessToken } from "./authApi";

const BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080") + "/api/v1";

function authHeaders() {
  return { Authorization: `Bearer ${getAccessToken()}` };
}

// 보유 ?�료 기반 ?�시??추천 목록 조회
export async function fetchRecommendedRecipes(page = 0, size = 10) {
  const response = await fetch(`${BASE_URL}/recipes/recommend?page=${page}&size=${size}`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("추천 ?�시?��? 불러?��? 못했?�니??");
  }
  return response.json();
}

// ?�시???�세 조회 (FR-24) ??공용 조회, ?�큰 불필??
export async function fetchRecipeDetail(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}`);

  if (!response.ok) {
    throw new Error("?�시???�세 ?�보�?불러?��? 못했?�니??");
  }

  return response.json();
}

// AutoML 추천 ?�시??
export async function fetchComboRecommendations() {
  const response = await fetch(`${BASE_URL}/recipes/combo-recommend`, {
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("?�외??조합 추천??불러?��? 못했?�니??");
  return response.json();
}

// ?�시??카테고리 ?�체 목록 (커�??�티 글?�기 ?�면 ?�롭?�운?? ??공용 조회, ?�큰 불필??
export async function fetchRecipeCategories() {
  const response = await fetch(`${BASE_URL}/recipes/categories`);
  if (!response.ok) throw new Error("카테고리 목록??불러?��? 못했?�니??");
  return response.json();
}

// ?�시??목록/검????공용 조회, ?�큰 불필??
export async function fetchRecipeList({ keyword = "", ingredientIds = [], page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({ page, size });
  if (keyword) params.set("keyword", keyword);
  if (ingredientIds.length > 0) params.set("ingredientIds", ingredientIds.join(","));

  const response = await fetch(`${BASE_URL}/recipes?${params.toString()}`);
  if (!response.ok) throw new Error("?�시??목록??불러?��? 못했?�니??");
  return response.json();
}
