import { getAccessToken } from "./authApi";
import { BASE_URL } from "./config";

function authHeaders(extra = {}) {
  return { Authorization: `Bearer ${getAccessToken()}`, ...extra };
}

// 레시피 기준 부족한 재료(장보기 리스트) 조회 (FR-30)
export async function fetchShoppingList(recipeId) {
  const response = await fetch(`${BASE_URL}/shopping-list?recipeId=${recipeId}`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("장보기 리스트를 불러오지 못했습니다.");
  }
  return response.json();
}

// 부족한 재료를 내 장보기 리스트에 담기
export async function addMissingIngredientsToMyList(recipeId) {
  const response = await fetch(`${BASE_URL}/shopping-list/items?recipeId=${recipeId}`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("장보기 리스트에 담지 못했습니다.");
}

// 내 장보기 리스트 전체 조회
export async function fetchMyShoppingList() {
  const response = await fetch(`${BASE_URL}/shopping-list/me`, {
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("장보기 리스트를 불러오지 못했습니다.");
  return response.json();
}

export async function checkShoppingItem(itemId) {
  const response = await fetch(`${BASE_URL}/shopping-list/items/${itemId}/check`, {
    method: "PATCH",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("체크 처리에 실패했습니다.");
}

export async function uncheckShoppingItem(itemId) {
  const response = await fetch(`${BASE_URL}/shopping-list/items/${itemId}/uncheck`, {
    method: "PATCH",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("체크 해제에 실패했습니다.");
}

export async function deleteShoppingItem(itemId) {
  const response = await fetch(`${BASE_URL}/shopping-list/items/${itemId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("삭제에 실패했습니다.");
}

export async function addManualShoppingItem(payload) {
  const response = await fetch(`${BASE_URL}/shopping-list/items/manual`, {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || "재료를 담지 못했습니다.");
  }
}

export async function reorderShoppingItems(itemIds) {
  const response = await fetch(`${BASE_URL}/shopping-list/items/reorder`, {
    method: "PATCH",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({ itemIds }),
  });
  if (!response.ok) throw new Error("순서 변경에 실패했습니다.");
}

export async function deleteCheckedShoppingItems() {
  const response = await fetch(`${BASE_URL}/shopping-list/items/checked`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("선택 삭제에 실패했습니다.");
}

export async function deleteAllShoppingItems() {
  const response = await fetch(`${BASE_URL}/shopping-list/items`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("전체 삭제에 실패했습니다.");
}

export async function updateShoppingItemQuantity(itemId, quantity) {
  const response = await fetch(`${BASE_URL}/shopping-list/items/${itemId}/quantity`, {
    method: "PATCH",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({ quantity }),
  });
  if (!response.ok) throw new Error("수량 변경에 실패했습니다.");
}

export async function setAllShoppingItemsChecked(checked) {
  const response = await fetch(`${BASE_URL}/shopping-list/items/check-all`, {
    method: "PATCH",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({ checked }),
  });
  if (!response.ok) throw new Error("전체 선택 처리에 실패했습니다.");
}

export async function purchaseCheckedShoppingItems() {
  const response = await fetch(`${BASE_URL}/shopping-list/items/purchase`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "구매 처리에 실패했습니다.");
  }
  return response.json();
}