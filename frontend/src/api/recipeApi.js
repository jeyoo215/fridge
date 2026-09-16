import { getAccessToken } from "./authApi";
import { BASE_URL } from "./config";

function authHeaders() {
  return { Authorization: `Bearer ${getAccessToken()}` };
}

// ?�시???�세 조회 (FR-24) ??공용 조회, ?�큰 불필??
export async function fetchRecipeDetail(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}`);
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || "레시피 상세 정보를 불러오지 못했습니다.");
  }
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

export async function fetchComboRecommendations(onlyOwned = false) {
  const response = await fetch(`${BASE_URL}/recipes/combo-recommend?onlyOwned=${onlyOwned}`, {
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("추천 레시피를 불러오지 못했습니다.");
  return response.json();
}