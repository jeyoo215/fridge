import { getAccessToken } from "./authApi";

// PC에서 열면 localhost, 핸드폰 등 다른 기기에서 열면 그 기기가 접속한 주소(PC의 IP)를 그대로 사용
const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

function authHeaders(extra = {}) {
  return { Authorization: `Bearer ${getAccessToken()}`, ...extra };
}

// 좋아요 상태 + 개수 조회
export async function fetchLikeStatus(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/likes`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("좋아요 정보를 불러오지 못했습니다.");
  }
  return response.json();
}

// 좋아요 토글 (누를 때마다 켜짐/꺼짐)
export async function toggleLike(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/likes`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("좋아요 처리에 실패했습니다.");
  }
  return response.json();
}

// 스크랩 상태 + 개수 조회
export async function fetchScrapStatus(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/scraps`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("스크랩 정보를 불러오지 못했습니다.");
  }
  return response.json();
}

// 스크랩 토글
export async function toggleScrap(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/scraps`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("스크랩 처리에 실패했습니다.");
  }
  return response.json();
}

// 내가 스크랩한 레시피 목록 (마이페이지용)
export async function fetchMyScraps() {
  const response = await fetch(`${BASE_URL}/users/me/scraps`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("스크랩 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 인기 레시피 목록 (좋아요순 또는 리뷰순) — 로그인 여부와 무관한 공용 조회라 토큰 불필요
export async function fetchPopularRecipes(sortBy = "likes") {
  const response = await fetch(`${BASE_URL}/recipes/popular?sortBy=${sortBy}`);
  if (!response.ok) {
    throw new Error("인기 레시피를 불러오지 못했습니다.");
  }
  return response.json();
}

// --- 만들어본 레시피 ---

// "만들었어요" 상태 + 개수 조회
export async function fetchMadeStatus(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/cook-records`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("만들기 기록을 불러오지 못했습니다.");
  }
  return response.json();
}

// "만들었어요" 토글
export async function toggleMade(recipeId) {
  const response = await fetch(`${BASE_URL}/recipes/${recipeId}/cook-records`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("만들기 기록 처리에 실패했습니다.");
  }
  return response.json();
}

// 마이페이지: 내가 만들어본 레시피 목록
export async function fetchMyMadeRecipes() {
  const response = await fetch(`${BASE_URL}/users/me/made-recipes`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("만들어본 레시피 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 마이페이지: 내가 평가한 레시피 목록 (review 도메인 데이터를 가져옴)
export async function fetchMyReviewedRecipes() {
  const response = await fetch(`${BASE_URL}/users/me/recipe-reviews`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("평가한 레시피 목록을 불러오지 못했습니다.");
  }
  return response.json();
}
