const BASE_URL = "http://localhost:8080/api/v1";

// 내 냉장고 재료 목록 조회
// TODO: 로그인 기능 만들어지면 userId 파라미터 대신 JWT 토큰으로 대체
export async function fetchMyIngredients(userId) {
  const response = await fetch(`${BASE_URL}/users/me/ingredients?userId=${userId}`);
  if (!response.ok) {
    throw new Error("재료 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 재료 마스터 검색 (등록 화면 자동완성용)
export async function searchIngredients(keyword) {
  if (!keyword) return [];
  const response = await fetch(`${BASE_URL}/ingredients?keyword=${encodeURIComponent(keyword)}`);
  if (!response.ok) {
    throw new Error("재료 검색에 실패했습니다.");
  }
  return response.json();
}

// 재료 수동 등록
export async function registerIngredient(userId, payload) {
  const response = await fetch(`${BASE_URL}/users/me/ingredients?userId=${userId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error("재료 등록에 실패했습니다.");
  }
  return response.json();
}