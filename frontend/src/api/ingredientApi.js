// PC에서 열면 localhost, 핸드폰 등 다른 기기에서 열면 그 기기가 접속한 주소(PC의 IP)를 그대로 사용
const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

// 내 냉장고 재료 목록 조회
// TODO: 로그인 기능 만들어지면 userId 파라미터 대신 JWT 토큰으로 대체
export async function fetchMyIngredients(userId) {
  const response = await fetch(`${BASE_URL}/users/me/ingredients?userId=${userId}`);
  if (!response.ok) {
    throw new Error("재료 목록을 불러오지 못했습니다.");
  }
  return response.json();
}

// 재료 마스터 검색 (등록 화면 자동완성용)
export async function searchIngredients(keyword) {
  if (!keyword) return [];
  const response = await fetch(`${BASE_URL}/ingredients?keyword=${encodeURIComponent(keyword)}`);
  if (!response.ok) {
    throw new Error("재료 검색에 실패했습니다.");
  }
  return response.json();
}

// 카메라로 찍은 재료 사진 인식 요청 (오인식 방지를 위해 등록은 별도로 확정)
export async function recognizeIngredientImage(userId, file) {
  const formData = new FormData();
  formData.append("image", file);

  const response = await fetch(`${BASE_URL}/users/me/ingredients/recognize?userId=${userId}`, {
    method: "POST",
    body: formData,
  });
  if (!response.ok) {
    throw new Error("이미지 인식에 실패했습니다.");
  }
  return response.json();
}

// 재료 수동 등록
export async function registerIngredient(userId, payload) {
  const response = await fetch(`${BASE_URL}/users/me/ingredients?userId=${userId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error("재료 등록에 실패했습니다.");
  }
  return response.json();
}

// 재료 수정 (수량/유통기한)
export async function updateIngredient(userId, userIngredientId, payload) {
  const response = await fetch(
    `${BASE_URL}/users/me/ingredients/${userIngredientId}?userId=${userId}`,
    {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    }
  );
  if (!response.ok) {
    throw new Error("재료 수정에 실패했습니다.");
  }
}

// 재료 소진 처리
export async function consumeIngredient(userId, userIngredientId) {
  const response = await fetch(
    `${BASE_URL}/users/me/ingredients/${userIngredientId}/consume?userId=${userId}`,
    { method: "PATCH" }
  );
  if (!response.ok) {
    throw new Error("소진 처리에 실패했습니다.");
  }
}

// 재료 폐기 처리
export async function discardIngredient(userId, userIngredientId) {
  const response = await fetch(
    `${BASE_URL}/users/me/ingredients/${userIngredientId}/discard?userId=${userId}`,
    { method: "PATCH" }
  );
  if (!response.ok) {
    throw new Error("폐기 처리에 실패했습니다.");
  }
}

// 재료 삭제
export async function deleteIngredient(userId, userIngredientId) {
  const response = await fetch(
    `${BASE_URL}/users/me/ingredients/${userIngredientId}?userId=${userId}`,
    { method: "DELETE" }
  );
  if (!response.ok) {
    throw new Error("삭제에 실패했습니다.");
  }
}
