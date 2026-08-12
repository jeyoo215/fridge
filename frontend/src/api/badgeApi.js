const BASE_URL = "http://localhost:8080/api/v1";

export async function fetchMyBadges(userId) {
  const response = await fetch(`${BASE_URL}/users/me/badges?userId=${userId}`);
  if (!response.ok) throw new Error("뱃지 목록을 불러오지 못했습니다.");
  return response.json();
}

export async function fetchMyStreak(userId) {
  const response = await fetch(`${BASE_URL}/users/me/streak?userId=${userId}`);
  if (!response.ok) throw new Error("스트릭 정보를 불러오지 못했습니다.");
  return response.json();
}