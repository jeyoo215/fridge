import { getAccessToken } from "./authApi";
import { BASE_URL } from "./config";

// 로그인 토큰을 담은 요청 헤더. 매번 이렇게 안 쓰려고 헬퍼로 뺌.
function authHeaders(extra = {}) {
  return { Authorization: `Bearer ${getAccessToken()}`, ...extra };
}

// 내 냉장고 재료 목록 조회
export async function fetchMyIngredients() {
  const response = await fetch(`${BASE_URL}/users/me/ingredients`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("재료 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 재료 마스터 검색 (등록 화면 자동완성용) — 로그인 여부와 무관한 공용 조회라 토큰 불필요
export async function searchIngredients(keyword) {
  if (!keyword) return [];
  const response = await fetch(`${BASE_URL}/ingredients?keyword=${encodeURIComponent(keyword)}`);
  if (!response.ok) {
    throw new Error("재료 검색에 실패했습니다.");
  }
  return response.json();
}

// 카메라로 찍은 재료 사진 인식 요청 (오인식 방지를 위해 등록은 별도로 확정)
export async function recognizeIngredientImage(file) {
  const formData = new FormData();
  formData.append("image", file);

  const response = await fetch(`${BASE_URL}/users/me/ingredients/recognize`, {
    method: "POST",
    headers: authHeaders(), // FormData 쓸 땐 Content-Type을 직접 안 넣어야 브라우저가 알아서 boundary를 채워줌
    body: formData,
  });
  if (!response.ok) {
    throw new Error("이미지 인식에 실패했습니다.");
  }
  return response.json();
}

// 재료 카테고리 전체 목록 (새 재료 등록용 드롭다운) — 공용 조회, 토큰 불필요
export async function fetchIngredientCategories() {
  const response = await fetch(`${BASE_URL}/ingredients/categories`);
  if (!response.ok) {
    throw new Error("카테고리 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 재료 마스터에 없는 재료를 새로 등록 (등록된 재료 정보를 반환) — 공용 마스터 데이터라 토큰 불필요
export async function createIngredient(payload) {
  const response = await fetch(`${BASE_URL}/ingredients`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || "새 재료 등록에 실패했습니다.");
  }
  return response.json();
}

// 재료 수동 등록
export async function registerIngredient(payload) {
  const response = await fetch(`${BASE_URL}/users/me/ingredients`, {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error("재료 등록에 실패했습니다.");
  }
  return response.json();
}

// 재료 수정 (수량/유통기한)
export async function updateIngredient(userIngredientId, payload) {
  const response = await fetch(`${BASE_URL}/users/me/ingredients/${userIngredientId}`, {
    method: "PATCH",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error("재료 수정에 실패했습니다.");
  }
}

// 재료 삭제 (사용완료/폐기 구분 없이 "삭제" 하나로 통합)
export async function deleteIngredient(userIngredientId) {
  const response = await fetch(`${BASE_URL}/users/me/ingredients/${userIngredientId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("삭제에 실패했습니다.");
  }
}
