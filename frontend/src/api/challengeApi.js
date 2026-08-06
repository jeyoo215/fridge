const BASE_URL = "http://localhost:8080/api/v1";

export async function startChallenge(userId, days) {
  const response = await fetch(`${BASE_URL}/challenges?userId=${userId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ days }),
  });
  if (!response.ok) throw new Error("챌린지를 시작하지 못했습니다.");
  return response.json();
}

export async function fetchChallengeStatus(challengeId) {
  const response = await fetch(`${BASE_URL}/challenges/${challengeId}`);
  if (!response.ok) throw new Error("챌린지 상태를 불러오지 못했습니다.");
  return response.json();
}