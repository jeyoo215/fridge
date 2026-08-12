const BASE_URL = "http://localhost:8080/api/v1";

export async function startChallenge(userId, days) {
  const response = await fetch(`${BASE_URL}/challenges?userId=${userId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ days }),
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
  if (response.status === 404) return null; // 진행중인 챌린지 없음 (정상 케이스)
  if (!response.ok) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || "챌린지 상태를 불러오지 못했습니다.");
  }
  return response.json();
}