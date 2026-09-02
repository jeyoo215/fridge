import { getAccessToken } from "./authApi";

const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

function authHeaders() {
  return { Authorization: `Bearer ${getAccessToken()}` };
}

export async function fetchMyBadges() {
  const response = await fetch(`${BASE_URL}/users/me/badges`, { headers: authHeaders() });
  if (!response.ok) throw new Error("뱃지 목록을 불러오지 못했습니다.");
  return response.json();
}

export async function fetchMyStreak() {
  const response = await fetch(`${BASE_URL}/users/me/streak`, { headers: authHeaders() });
  if (!response.ok) throw new Error("스트릭 정보를 불러오지 못했습니다.");
  return response.json();
}
