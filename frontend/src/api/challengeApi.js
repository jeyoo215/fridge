const BASE_URL = `http://${window.location.hostname}:8080/api/v1`; // 다른 파일들이랑 통일

export async function startChallenge(userId, { days, type, targetIngredientIds }) {
  const response = await fetch(`${BASE_URL}/challenges?userId=${userId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ days, type, targetIngredientIds }),
  });
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || "챌린지를 시작하지 못했습니다.");
  }
  return response.json();
}

export async function fetchChallengeStatus(challengeId) {
  const response = await fetch(`${BASE_URL}/challenges/${challengeId}`);
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || "챌린지 상태를 불러오지 못했습니다.");
  }
  return response.json();
}

export async function fetchActiveChallenge(userId) {
  const response = await fetch(`${BASE_URL}/challenges/me?userId=${userId}`);
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || "챌린지 상태를 불러오지 못했습니다.");
  }
  const text = await response.text();
  return text ? JSON.parse(text) : null; // 빈 body면 null (진행중인 챌린지 없음)
}

export async function abortChallenge(challengeId) {
  const response = await fetch(`${BASE_URL}/challenges/${challengeId}/abort`, {
    method: "PATCH",
  });
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || "챌린지를 중단하지 못했습니다.");
  }
  return response.json();
}

export async function fetchChallengeHistory(userId, page = 0, size = 5) {
  const response = await fetch(`${BASE_URL}/challenges/me/history?userId=${userId}&page=${page}&size=${size}`);
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || "챌린지 기록을 불러오지 못했습니다.");
  }
  return response.json();
}