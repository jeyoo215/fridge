const BASE_URL = "http://localhost:8080/api/v1";

// 레시피 기준 부족한 재료(장보기 리스트) 조회 (FR-30)
export async function fetchShoppingList(userId, recipeId) {
  const response = await fetch(`${BASE_URL}/shopping-list?userId=${userId}&recipeId=${recipeId}`);

  if (!response.ok) {
    throw new Error("장보기 리스트를 불러오지 못했습니다.");
  }

  return response.json();
}

// 부족한 재료를 내 장보기 리스트에 담기
export async function addMissingIngredientsToMyList(userId, recipeId) {
  const response = await fetch(
    `${BASE_URL}/shopping-list/items?userId=${userId}&recipeId=${recipeId}`,
    { method: "POST" }
  );
  if (!response.ok) throw new Error("장보기 리스트에 담지 못했습니다.");
}

// 내 장보기 리스트 전체 조회
export async function fetchMyShoppingList(userId) {
  const response = await fetch(`${BASE_URL}/shopping-list/me?userId=${userId}`);
  if (!response.ok) throw new Error("장보기 리스트를 불러오지 못했습니다.");
  return response.json();
}

export async function checkShoppingItem(userId, itemId) {
  const response = await fetch(
    `${BASE_URL}/shopping-list/items/${itemId}/check?userId=${userId}`,
    { method: "PATCH" }
  );
  if (!response.ok) throw new Error("체크 처리에 실패했습니다.");
}

export async function uncheckShoppingItem(userId, itemId) {
  const response = await fetch(
    `${BASE_URL}/shopping-list/items/${itemId}/uncheck?userId=${userId}`,
    { method: "PATCH" }
  );
  if (!response.ok) throw new Error("체크 해제에 실패했습니다.");
}

export async function deleteShoppingItem(userId, itemId) {
  const response = await fetch(
    `${BASE_URL}/shopping-list/items/${itemId}?userId=${userId}`,
    { method: "DELETE" }
  );
  if (!response.ok) throw new Error("삭제에 실패했습니다.");
}