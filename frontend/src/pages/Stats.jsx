import { useEffect, useState } from "react";
import { fetchMonthlyStats } from "../api/statsApi";
import "./Stats.css";

const TEMP_USER_ID = 1; // TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체

function currentYearMonth() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

// "2026-08" -> "2026년 8월"
function formatYearMonthLabel(yearMonth) {
  const [year, month] = yearMonth.split("-");
  return `${year}년 ${Number(month)}월`;
}

export default function Stats() {
  const [yearMonth, setYearMonth] = useState(currentYearMonth());
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    fetchMonthlyStats(TEMP_USER_ID, yearMonth)
      .then(setStats)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [yearMonth]);

  const changeMonth = (diff) => {
    const [year, month] = yearMonth.split("-").map(Number);
    const date = new Date(year, month - 1 + diff, 1);
    setYearMonth(`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`);
  };

  return (
    <div className="stats-page-container">
      <h2 className="stats-page-title">📊 이번 달 냉장고 리포트</h2>

      <div className="stats-month-nav">
        <button onClick={() => changeMonth(-1)}>◀</button>
        <span>{formatYearMonthLabel(yearMonth)}</span>
        <button onClick={() => changeMonth(1)} disabled={yearMonth >= currentYearMonth()}>
          ▶
        </button>
      </div>

      {loading && <p className="stats-status">불러오는 중...</p>}
      {error && <p className="stats-status">{error}</p>}

      {stats && !loading && (
        <>
          <div className="stats-summary-grid">
            <div className="stats-summary-card">
              <span className="stats-summary-value">{stats.consumedCount}개</span>
              <span className="stats-summary-label">다 쓴 재료</span>
            </div>
            <div className="stats-summary-card stats-summary-card-danger">
              <span className="stats-summary-value">{stats.discardedCount}개</span>
              <span className="stats-summary-label">폐기한 재료</span>
            </div>
          </div>

          <div className="stats-highlight-card">
            <p className="stats-highlight-label">이번 달 절약한 금액</p>
            <p className="stats-highlight-value">
              {Number(stats.estimatedSavedAmount).toLocaleString()}원
            </p>
            <p className="stats-highlight-note">
              {stats.savedAmountPartiallyEstimated
                ? "* 가격을 입력 안 한 재료는 평균값으로 계산했어요 (일부 추정치 포함)"
                : "* 등록하신 실제 가격 그대로 계산했어요"}
            </p>
          </div>

          <div className="stats-highlight-card stats-highlight-card-green">
            <p className="stats-highlight-label">탄소 절감 효과 (추정)</p>
            <p className="stats-highlight-value">{stats.estimatedCo2ReductionKg}kg CO₂</p>
            <p className="stats-highlight-equivalent">🚗 {stats.equivalentDescription}</p>
            <p className="stats-highlight-note">* 폐기 없이 다 쓴 재료 기준 추정치예요</p>
          </div>
        </>
      )}
    </div>
  );
}
