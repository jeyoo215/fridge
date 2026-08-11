const HOST_URL = `http://${window.location.hostname}:8080`;
const BASE_URL = `${HOST_URL}/api/v1`;

// 업로드 응답의 url은 상대경로("/media/community/...")라서, <img>/<video> src로 쓰려면 호스트를 붙여야 함
export function toMediaSrc(relativeUrl) {
  if (!relativeUrl) return "";
  return `${HOST_URL}${relativeUrl}`;
}

// 게시글 섹션에 첨부할 이미지/동영상 업로드. 성공하면 { url, mediaType } 반환.
export async function uploadCommunityMedia(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`${BASE_URL}/community/media`, {
    method: "POST",
    body: formData,
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "파일 업로드에 실패했습니다.");
  }
  return response.json();
}

// 커뮤니티 게시글 목록 (최신순, 페이지당 10개)
export async function fetchCommunityPosts(page = 0, size = 10) {
  const response = await fetch(`${BASE_URL}/community/posts?page=${page}&size=${size}`);
  if (!response.ok) {
    throw new Error("게시글 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 게시글 상세
export async function fetchCommunityPost(postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}`);
  if (!response.ok) {
    throw new Error("게시글을 불러오지 못했습니다.");
  }
  return response.json();
}

// 게시글 작성 (제목 + 섹션 목록)
// TODO: 로그인 기능 만들어지면 userId 파라미터 대신 JWT 토큰으로 대체
export async function createCommunityPost(userId, payload) {
  const response = await fetch(`${BASE_URL}/community/posts?userId=${userId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error("게시글 등록에 실패했습니다.");
  }
  return response.json();
}

// 게시글 수정 (본인 글만). 제목/섹션 전체를 새 내용으로 교체.
export async function updateCommunityPost(userId, postId, payload) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}?userId=${userId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "게시글 수정에 실패했습니다.");
  }
}

// 게시글 삭제 (본인 글만)
export async function deleteCommunityPost(userId, postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}?userId=${userId}`, {
    method: "DELETE",
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "게시글 삭제에 실패했습니다.");
  }
}

// 스크랩 상태 + 총 개수 조회
export async function fetchCommunityPostScrapStatus(userId, postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/scraps?userId=${userId}`);
  if (!response.ok) {
    throw new Error("스크랩 상태를 불러오지 못했습니다.");
  }
  return response.json();
}

// 스크랩 토글
export async function toggleCommunityPostScrap(userId, postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/scraps?userId=${userId}`, {
    method: "POST",
  });
  if (!response.ok) {
    throw new Error("스크랩 처리에 실패했습니다.");
  }
  return response.json();
}

// 좋아요 상태 + 총 개수 조회
export async function fetchCommunityPostLikeStatus(userId, postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/likes?userId=${userId}`);
  if (!response.ok) {
    throw new Error("좋아요 상태를 불러오지 못했습니다.");
  }
  return response.json();
}

// 좋아요 토글
export async function toggleCommunityPostLike(userId, postId) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/likes?userId=${userId}`, {
    method: "POST",
  });
  if (!response.ok) {
    throw new Error("좋아요 처리에 실패했습니다.");
  }
  return response.json();
}
