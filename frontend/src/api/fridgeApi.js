const BASE_URL = "http://localhost:8080/api/v1";

// 냉장고 이름 조회
export async function fetchFridgeName(userId) {
  const response = await fetch(`${BASE_URL}/users/me/fridge?userId=${userId}`);
  if (!response.ok) {
    throw new Error("냉장고 이름을 불러오지 못했습니다.");
  }
  const data = await response.json();
  return data.fridgeName;
}

// 냉장고 이름 수정
export async function updateFridgeName(userId, fridgeName) {
  const response = await fetch(`${BASE_URL}/users/me/fridge?userId=${userId}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ fridgeName }),
  });
  if (!response.ok) {
    throw new Error("냉장고 이름 수정에 실패했습니다.");
  }
  const data = await response.json();
  return data.fridgeName;
}
