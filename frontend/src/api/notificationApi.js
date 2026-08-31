import { getAccessToken } from "./authApi";

const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

function authHeaders(extra = {}) {
  return { Authorization: `Bearer ${getAccessToken()}`, ...extra };
}

export async function fetchMyNotifications() {
  const response = await fetch(`${BASE_URL}/notifications`, { headers: authHeaders() });
  if (!response.ok) throw new Error("알림 목록을 불러오지 못했습니다.");
  return response.json();
}

export async function fetchUnreadNotificationCount() {
  const response = await fetch(`${BASE_URL}/notifications/unread-count`, { headers: authHeaders() });
  if (!response.ok) throw new Error("알림 개수를 불러오지 못했습니다.");
  return response.json();
}

export async function markNotificationAsRead(notificationId) {
  const response = await fetch(`${BASE_URL}/notifications/${notificationId}/read`, {
    method: "PATCH",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("알림 읽음 처리에 실패했습니다.");
}

export async function markAllNotificationsAsRead() {
  const response = await fetch(`${BASE_URL}/notifications/read-all`, {
    method: "PATCH",
    headers: authHeaders(),
  });
  if (!response.ok) throw new Error("알림 전체 읽음 처리에 실패했습니다.");
}
