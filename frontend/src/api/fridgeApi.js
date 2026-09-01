import { getAccessToken } from "./authApi";

const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

function authHeaders(extra = {}) {
  return { Authorization: `Bearer ${getAccessToken()}`, ...extra };
}

// 냉장고 이름 조회
export async function fetchFridgeName() {
  const response = await fetch(`${BASE_URL}/users/me/fridge`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("냉장고 이름을 불러오지 못했습니다.");
  }
  const data = await response.json();
  return data.fridgeName;
}

// 냉장고 이름 수정
export async function updateFridgeName(fridgeName) {
  const response = await fetch(`${BASE_URL}/users/me/fridge`, {
    method: "PATCH",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({ fridgeName }),
  });
  if (!response.ok) {
    throw new Error("냉장고 이름 수정에 실패했습니다.");
  }
  const data = await response.json();
  return data.fridgeName;
}


// ================================== 
// ========== 냉장고 이미지 ===========
// ================================== 

// 냉장고에 배치된 재료 목록 조회
export async function fetchFridgeItems() {
  const response = await fetch(`${BASE_URL}/fridge/items`, {
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("냉장고 재료를 불러오지 못했습니다.");
  return response.json();
}

// 새 재료 등록 + 배치
export async function createFridgeItem(payload) {
  const response = await fetch(`${BASE_URL}/fridge/items`, {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(payload),
  });
  if (!response.ok) throw new Error("재료 배치에 실패했습니다.");
  return response.json();
}

// 기존 보유재료 배치
export async function placeFridgeItem(payload) {
  const response = await fetch(`${BASE_URL}/fridge/items/place`, {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(payload),
  });
  if (!response.ok) throw new Error("배치에 실패했습니다.");
  return response.json();
}

// 위치/구역 이동
export async function moveFridgeItem(fridgeItemId, posX, posY, zone) {
  const params = new URLSearchParams({ posX, posY, zone });
  const response = await fetch(`${BASE_URL}/fridge/items/${fridgeItemId}/move?${params}`, {
    method: "PATCH",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("이동에 실패했습니다.");
}

// 배치 제거
export async function removeFridgeItem(fridgeItemId) {
  const response = await fetch(`${BASE_URL}/fridge/items/${fridgeItemId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("삭제에 실패했습니다.");
}

export async function uploadImage(file) {
  const form = new FormData();
  form.append("file", file);
  const response = await fetch(`${BASE_URL}/community/media`, {
    method: "POST",
    headers: authHeaders(),
    body: form,
  });
  if (!response.ok) throw new Error("이미지 업로드에 실패했습니다.");
  const data = await response.json();
  return data.url;
}


const API_BASE = `http://${window.location.hostname}:8080`;

export async function resizeFridgeItem(fridgeItemId, scale) {
  const response = await fetch(
    `${API_BASE}/api/v1/fridge/items/${fridgeItemId}/resize?scale=${scale}`,
    {
      method: "PATCH",
    }
  );
  if (!response.ok) throw new Error("크기 변경 실패");
}