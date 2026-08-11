const BASE_URL = `http://${window.location.hostname}:8080/api/v1`;

// 월간 통계 조회 (yearMonth 생략하면 이번 달)
export async function fetchMonthlyStats(userId, yearMonth) {
  const query = yearMonth ? `&yearMonth=${yearMonth}` : "";
  const response = await fetch(`${BASE_URL}/users/me/stats/monthly?userId=${userId}${query}`);
  if (!response.ok) {
    throw new Error("통계를 불러오지 못했습니다.");
  }
  return response.json();
}
