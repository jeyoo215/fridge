import { useState } from "react";
import { getAccessToken } from "../api/authApi";
import "./Admin.css";
import { BASE_URL } from "../api/config";

export default function Admin() {
  const [status, setStatus] = useState(null);

  const handleRefreshCombo = async () => {
    setStatus("실행 중...");
    try {
      const response = await fetch(`${BASE_URL}/admin/combo-recommend/refresh`, {
        method: "POST",
        headers: { Authorization: `Bearer ${getAccessToken()}` },
      });
      if (!response.ok) throw new Error("실행 실패");
      setStatus("배치가 백그라운드에서 실행 중입니다.");
    } catch (err) {
      setStatus(err.message);
    }
  };

  return (
    <div className="admin-container">
      <h2 className="admin-title">관리자 페이지</h2>

      <section className="admin-section">
        <h3>조합 추천 배치</h3>
        <p className="admin-section-desc">
          매일 새벽 자동 실행되는 배치를 지금 바로 실행합니다.
        </p>
        <button className="admin-action-button" onClick={handleRefreshCombo}>
          지금 실행
        </button>
        {status && <p className="admin-status">{status}</p>}
      </section>
    </div>
  );
}