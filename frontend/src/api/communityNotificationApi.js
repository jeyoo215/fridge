import { getAccessToken } from "./authApi";
import { BASE_URL } from "./config";

function authHeaders(extra = {}) {
  const token = getAccessToken();
  return token ? { Authorization: `Bearer ${token}`, ...extra } : { ...extra };
}

// 알림 벨 드롭다운 목록 (최신순, 최대 30개)
export async function fetchCommunityNotifications() {
  const response = await fetch(`${BASE_URL}/community/notifications`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("알림 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 벨 아이콘 위 안 읽은 개수 배지
export async function fetchCommunityNotificationUnreadCount() {
  const response = await fetch(`${BASE_URL}/community/notifications/unread-count`, {
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("알림 개수를 불러오지 못했습니다.");
  }
  const { unreadCount } = await response.json();
  return unreadCount;
}

// 알림 한 개 읽음 처리 (알림 항목 클릭 시)
export async function markCommunityNotificationRead(notificationId) {
  const response = await fetch(`${BASE_URL}/community/notifications/${notificationId}/read`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("알림 읽음 처리에 실패했습니다.");
  }
}

// 전부 읽음 처리 ("모두 읽음" 버튼)
export async function markAllCommunityNotificationsRead() {
  const response = await fetch(`${BASE_URL}/community/notifications/read-all`, {
    method: "POST",
    headers: authHeaders(),
  });
  if (!response.ok) {
    throw new Error("알림 읽음 처리에 실패했습니다.");
  }
}
