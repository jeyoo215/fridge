const BASE_URL = "http://localhost:8080/api/v1";

// 보유 재료 기반 레시피 추천 목록 조회
// TODO: 로그인 기능 만들어지면 userId 파라미터 대신 JWT 토큰으로 대체
export async function fetchRecommendedRecipes(userId) {
  const response = await fetch(`${BASE_URL}/recipes/recommend?userId=${userId}`);

  if (!response.ok) {
    throw new Error("추천 레시피를 불러오지 못했습니다.");
  }

  return response.json();
}