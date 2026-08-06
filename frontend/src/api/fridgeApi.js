// PC에서 열면 localhost, 핸드폰 등 다른 기기에서 열면 그 기기가 접속한 주소(PC의 IP)를 그대로 사용
const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

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
