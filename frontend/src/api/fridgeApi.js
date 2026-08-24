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
