import { getAccessToken } from "./authApi";

const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

function authHeaders(extra = {}) {
  return { Authorization: `Bearer ${getAccessToken()}`, ...extra };
}

// 마이페이지: 내 알레르기/기피 재료 목록
export async function fetchMyAllergyIngredients() {
  const response = await fetch(`${BASE_URL}/users/me/allergy-ingredients`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("알레르기/기피 재료를 불러오지 못했습니다.");
  }
  return response.json();
}

// 알레르기/기피 재료 직접 입력 등록
export async function registerAllergyIngredient(ingredientName, type) {
  const response = await fetch(`${BASE_URL}/users/me/allergy-ingredients`, {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({ ingredientName, type }),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "재료 등록에 실패했습니다.");
  }
  return response.json();
}

// 등록한 알레르기/기피 재료 삭제
export async function deleteAllergyIngredient(id) {
  const response = await fetch(`${BASE_URL}/users/me/allergy-ingredients/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "재료 삭제에 실패했습니다.");
  }
}

// 마이페이지 다중선택 화면에 보여줄 전체 조리도구 목록 (공용 마스터 데이터) — 토큰 불필요
export async function fetchAllCookingTools() {
  const response = await fetch(`${BASE_URL}/cooking-tools`);
  if (!response.ok) {
    throw new Error("조리도구 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 내가 보유한 조리도구 목록
export async function fetchMyTools() {
  const response = await fetch(`${BASE_URL}/users/me/tools`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("보유 조리도구를 불러오지 못했습니다.");
  }
  return response.json();
}

// 보유 조리도구 다중선택 결과 저장 (전체 교체 방식)
export async function updateMyTools(toolIds) {
  const response = await fetch(`${BASE_URL}/users/me/tools`, {
    method: "PUT",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({ toolIds }),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "조리도구 저장에 실패했습니다.");
  }
}

// ⚠️ 아래 3개(커뮤니티 내 활동 조회)는 community 도메인 컨트롤러(CommunityActivityController)가
//    아직 userId를 @RequestParam으로 받는 상태라, 그쪽 작업할 때 같이 맞춰서 고칠 예정. 지금은 그대로 둠.

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
