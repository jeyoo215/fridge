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

// 최근 N개월(기본 3개월) 통계를 한번에 가져옴 (월별 추이 그래프용)
// 기존 fetchMonthlyStats를 여러 번 호출해서 조합하는 방식이라 백엔드는 안 건드림
export async function fetchRecentMonthsStats(userId, monthCount = 3) {
  const now = new Date();
  const targets = [];
  for (let i = monthCount - 1; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    targets.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`);
  }
  return Promise.all(targets.map((ym) => fetchMonthlyStats(userId, ym)));
}
