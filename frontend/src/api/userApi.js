const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

// 마이페이지: 내 알레르기/기피 재료 목록
export async function fetchMyAllergyIngredients(userId) {
  const response = await fetch(`${BASE_URL}/users/me/allergy-ingredients?userId=${userId}`);
  if (!response.ok) {
    throw new Error("알레르기/기피 재료를 불러오지 못했습니다.");
  }
  return response.json();
}

// 알레르기/기피 재료 직접 입력 등록
export async function registerAllergyIngredient(userId, ingredientName, type) {
  const response = await fetch(`${BASE_URL}/users/me/allergy-ingredients?userId=${userId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ingredientName, type }),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "재료 등록에 실패했습니다.");
  }
  return response.json();
}

// 등록한 알레르기/기피 재료 삭제
export async function deleteAllergyIngredient(userId, id) {
  const response = await fetch(`${BASE_URL}/users/me/allergy-ingredients/${id}?userId=${userId}`, {
    method: "DELETE",
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "재료 삭제에 실패했습니다.");
  }
}

// 마이페이지 다중선택 화면에 보여줄 전체 조리도구 목록 (공용 마스터 데이터)
export async function fetchAllCookingTools() {
  const response = await fetch(`${BASE_URL}/cooking-tools`);
  if (!response.ok) {
    throw new Error("조리도구 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 내가 보유한 조리도구 목록
export async function fetchMyTools(userId) {
  const response = await fetch(`${BASE_URL}/users/me/tools?userId=${userId}`);
  if (!response.ok) {
    throw new Error("보유 조리도구를 불러오지 못했습니다.");
  }
  return response.json();
}

// 보유 조리도구 다중선택 결과 저장 (전체 교체 방식)
export async function updateMyTools(userId, toolIds) {
  const response = await fetch(`${BASE_URL}/users/me/tools?userId=${userId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ toolIds }),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "조리도구 저장에 실패했습니다.");
  }
}

// 마이페이지 "내 활동": 내가 스크랩한 게시글 목록
export async function fetchMyCommunityScraps(userId) {
  const response = await fetch(`${BASE_URL}/users/me/community/scraps?userId=${userId}`);
  if (!response.ok) {
    throw new Error("스크랩한 게시글을 불러오지 못했습니다.");
  }
  return response.json();
}

// 마이페이지 "내 활동": 내가 좋아요한 게시글 목록
export async function fetchMyCommunityLikes(userId) {
  const response = await fetch(`${BASE_URL}/users/me/community/likes?userId=${userId}`);
  if (!response.ok) {
    throw new Error("좋아요한 게시글을 불러오지 못했습니다.");
  }
  return response.json();
}

// 마이페이지 "내 활동": 내가 댓글단 게시글 목록
export async function fetchMyCommunityComments(userId) {
  const response = await fetch(`${BASE_URL}/users/me/community/comments?userId=${userId}`);
  if (!response.ok) {
    throw new Error("댓글단 게시글을 불러오지 못했습니다.");
  }
  return response.json();
}
