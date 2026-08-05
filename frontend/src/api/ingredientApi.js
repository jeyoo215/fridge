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
