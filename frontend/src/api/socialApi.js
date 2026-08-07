const BASE_URL = "http://localhost:8080/api/v1";

// 좋아요 상태 + 개수 조회
export async function fetchLikeStatus(recipeId, userId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/likes?userId=${userId}`);
  if (!response.ok) {
    throw new Error("좋아요 정보를 불러오지 못했습니다.");
  }
  return response.json();
}

// 좋아요 토글 (누를 때마다 켜짐/꺼짐)
export async function toggleLike(recipeId, userId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/likes?userId=${userId}`, {
    method: "POST",
  });
  if (!response.ok) {
    throw new Error("좋아요 처리에 실패했습니다.");
  }
  return response.json();
}

// 스크랩 상태 + 개수 조회
export async function fetchScrapStatus(recipeId, userId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/scraps?userId=${userId}`);
  if (!response.ok) {
    throw new Error("스크랩 정보를 불러오지 못했습니다.");
  }
  return response.json();
}

// 스크랩 토글
export async function toggleScrap(recipeId, userId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/scraps?userId=${userId}`, {
    method: "POST",
  });
  if (!response.ok) {
    throw new Error("스크랩 처리에 실패했습니다.");
  }
  return response.json();
}

// 내가 스크랩한 레시피 목록 (마이페이지용)
export async function fetchMyScraps(userId) {
  const response = await fetch(`${BASE_URL}/users/me/scraps?userId=${userId}`);
  if (!response.ok) {
    throw new Error("스크랩 목록을 불러오지 못했습니다.");
  }
  return response.json();
}
