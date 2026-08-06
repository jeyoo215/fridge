const BASE_URL = "http://localhost:8080/api/v1";

// 레시피 기준 부족한 재료(장보기 리스트) 조회 (FR-30)
export async function fetchShoppingList(userId, recipeId) {
  const response = await fetch(`${BASE_URL}/shopping-list?userId=${userId}&recipeId=${recipeId}`);

  if (!response.ok) {
    throw new Error("장보기 리스트를 불러오지 못했습니다.");
  }

  return response.json();
}