import { getAccessToken } from "./authApi";
import { BASE_URL } from "./config";

function authHeaders() {
  return { Authorization: `Bearer ${getAccessToken()}` };
}

export async function fetchMyBadges() {
  const response = await fetch(`${BASE_URL}/users/me/badges`, { headers: authHeaders() });
  if (!response.ok) throw new Error("뱃�? 목록??불러?��? 못했?�니??");
  return response.json();
}

export async function fetchMyStreak() {
  const response = await fetch(`${BASE_URL}/users/me/streak`, { headers: authHeaders() });
  if (!response.ok) throw new Error("?�트�??�보�?불러?��? 못했?�니??");
  return response.json();
}
