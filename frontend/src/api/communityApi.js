import { getAccessToken } from "./authApi";
import { HOST, BASE_URL } from "./config";

// 토큰 있으면 헤더 붙이고, 없으면 아예 생략 (비로그인도 볼 수 있는 화면이 있어서)
function authHeaders(extra = {}) {
  const token = getAccessToken();
  return token ? { Authorization: `Bearer ${token}`, ...extra } : { ...extra };
}

// 업로드 응답의 url은 상대경로("/media/community/...")라서, <img>/<video> src로 쓰려면 호스트를 붙여야 함
// 단, 레시피 승격/외부 데이터(식약처 등)는 이미 절대경로 URL이라 그대로 써야 함
export function toMediaSrc(url) {
  if (!url) return "";
  if (/^https?:\/\//i.test(url)) return url;
  return `${HOST}${url}`;
}

// 게시글 섹션에 첨부할 이미지/동영상 업로드. 성공하면 { url, mediaType } 반환.
export async function uploadCommunityMedia(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`${BASE_URL}/community/media`, {
    method: "POST",
    headers: authHeaders(),
    body: formData,
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "파일 업로드에 실패했습니다.");
  }
  return response.json();
}

// 커뮤니티 게시글 목록 (최신순, 페이지당 10개). boardType 생략하면 레시피 게시판(기존과 동일 동작).
// prefix는 잡담 게시판 말머리 필터용, keyword는 제목 검색용 (모두 선택값).
// 챌린지 게시판 접근 자격 확인은 로그인 토큰이 있으면 자동으로 서버가 인식함.
export async function fetchCommunityPosts(page = 0, size = 10, sortBy = "latest", boardType = "RECIPE", { prefix, keyword } = {}) {
  const params = new URLSearchParams({ page, size, sortBy, boardType });
  if (prefix) params.set("prefix", prefix);
  if (keyword) params.set("keyword", keyword);
  const response = await fetch(`${BASE_URL}/community/posts?${params.toString()}`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "게시글 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 게시글 상세. 챌린지 게시판 글일 때 접근 자격 확인은 로그인 토큰이 있으면 자동으로 서버가 인식함.
export async function fetchCommunityPost(postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "게시글을 불러오지 못했습니다.");
  }
  return response.json();
}

// 게시글 작성 (제목 + 섹션 목록)
export async function createCommunityPost(payload) {
  const response = await fetch(`${BASE_URL}/community/posts`, {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error("게시글 등록에 실패했습니다.");
  }
  return response.json();
}

// 게시글 수정 (본인 글만). 제목/섹션 전체를 새 내용으로 교체.
export async function updateCommunityPost(postId, payload) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}`, {
    method: "PUT",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "게시글 수정에 실패했습니다.");
  }
}

// 게시글 삭제 (본인 글만)
export async function deleteCommunityPost(postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "게시글 삭제에 실패했습니다.");
  }
}

// 스크랩 상태 + 총 개수 조회
export async function fetchCommunityPostScrapStatus(postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/scraps`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("스크랩 상태를 불러오지 못했습니다.");
  }
  return response.json();
}

// 스크랩 토글
export async function toggleCommunityPostScrap(postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/scraps`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("스크랩 처리에 실패했습니다.");
  }
  return response.json();
}

// 좋아요 상태 + 총 개수 조회
export async function fetchCommunityPostLikeStatus(postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/likes`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("좋아요 상태를 불러오지 못했습니다.");
  }
  return response.json();
}

// 좋아요 토글
export async function toggleCommunityPostLike(postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/likes`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("좋아요 처리에 실패했습니다.");
  }
  return response.json();
}

// 댓글 목록 (등록순) — 공용 조회, 토큰 불필요
export async function fetchCommunityPostComments(postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/comments`);
  if (!response.ok) {
    throw new Error("댓글 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 댓글/대댓글 등록. parentCommentId를 주면 그 댓글에 대한 대댓글로 등록됨.
export async function createCommunityPostComment(postId, content, parentCommentId = null) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/comments`, {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({ content, parentCommentId }),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "댓글 등록에 실패했습니다.");
  }
  return response.json();
}

// 댓글 삭제 (본인 댓글만)
export async function deleteCommunityPostComment(commentId) {
  const response = await fetch(`${BASE_URL}/community/comments/${commentId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "댓글 삭제에 실패했습니다.");
  }
}
