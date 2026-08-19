// PC에서 열면 localhost, 핸드폰 등 다른 기기에서 열면 그 기기가 접속한 주소(PC의 IP)를 그대로 사용
const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

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

export async function addManualShoppingItem(userId, payload) {
  const response = await fetch(`${BASE_URL}/shopping-list/items/manual?userId=${userId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || "재료를 담지 못했습니다.");
  }
}

export async function reorderShoppingItems(userId, itemIds) {
  const response = await fetch(`${BASE_URL}/shopping-list/items/reorder?userId=${userId}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ itemIds }),
  });
  if (!response.ok) throw new Error("순서 변경에 실패했습니다.");
}

export async function deleteCheckedShoppingItems(userId) {
  const response = await fetch(`${BASE_URL}/shopping-list/items/checked?userId=${userId}`, {
    method: "DELETE",
  });
  if (!response.ok) throw new Error("선택 삭제에 실패했습니다.");
}

export async function deleteAllShoppingItems(userId) {
  const response = await fetch(`${BASE_URL}/shopping-list/items?userId=${userId}`, {
    method: "DELETE",
  });
  if (!response.ok) throw new Error("전체 삭제에 실패했습니다.");
}

export async function updateShoppingItemQuantity(userId, itemId, quantity) {
  const response = await fetch(
    `${BASE_URL}/shopping-list/items/${itemId}/quantity?userId=${userId}`,
    {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ quantity }),
    }
  );
  if (!response.ok) throw new Error("수량 변경에 실패했습니다.");
}