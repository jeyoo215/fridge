import { getAccessToken } from "./authApi";
import { BASE_URL } from "./config";

// 신고 사유 옵션. 백엔드 CommunityReportService.VALID_REASONS와 정확히 일치해야 함.
export const REPORT_REASONS = ["스팸/광고", "욕설/혐오", "음란물", "허위정보", "기타"];

function authHeaders(extra = {}) {
  return { Authorization: `Bearer ${getAccessToken()}`, ...extra };
}

// 게시글 신고
export async function reportPost(postId, reason) {
  const response = await fetch(`${BASE_URL}/community/posts/${postId}/reports`, {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({ reason }),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "신고에 실패했습니다.");
  }
}

// 댓글 신고
export async function reportComment(commentId, reason) {
  const response = await fetch(`${BASE_URL}/community/comments/${commentId}/reports`, {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({ reason }),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "신고에 실패했습니다.");
  }
}

// 관리자: 신고가 들어온 게시글/댓글 목록
export async function fetchReportedTargets() {
  const response = await fetch(`${BASE_URL}/admin/reports`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "신고 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 관리자: 신고가 정당함 — 대상 삭제
export async function resolveReportByDeleting(targetType, targetId) {
  const response = await fetch(`${BASE_URL}/admin/reports/${targetType}/${targetId}/delete`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "삭제 처리에 실패했습니다.");
  }
}

// 관리자: 신고가 부당함 — 숨김 해제하고 신고 목록에서 제거
export async function resolveReportByDismissing(targetType, targetId) {
  const response = await fetch(`${BASE_URL}/admin/reports/${targetType}/${targetId}/dismiss`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "기각 처리에 실패했습니다.");
  }
}
