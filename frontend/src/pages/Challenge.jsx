import { useEffect, useState } from "react";
import { startChallenge, fetchChallengeStatus, fetchActiveChallenge } from "../api/challengeApi";
import "./Challenge.css";

const TEMP_USER_ID = 1;

export default function Challenge() {
  const [checkingActive, setCheckingActive] = useState(true); // 초기 진행중 챌린지 확인 중
  const [daysInput, setDaysInput] = useState("7");
  const [challengeId, setChallengeId] = useState(null);
  const [status, setStatus] = useState(null);
  const [error, setError] = useState(null);

  // 화면 진입 시 진행중인 챌린지가 있으면 자동으로 불러옴 (새로고침해도 유지)
  useEffect(() => {
    fetchActiveChallenge(TEMP_USER_ID)
      .then((active) => {
        if (active) {
          setChallengeId(active.challengeId);
          setStatus(active);
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setCheckingActive(false));
  }, []);

  const handleDaysChange = (e) => {
    const digitsOnly = e.target.value.replace(/[^0-9]/g, "");
    const normalized = digitsOnly.replace(/^0+(?=\d)/, "");
    setDaysInput(normalized);
  };

  const handleStart = async () => {
    setError(null);
    const days = Number(daysInput);
    if (!days || days < 1) {
      setError("1일 이상 입력해주세요.");
      return;
    }
    try {
      const id = await startChallenge(TEMP_USER_ID, days);
      setChallengeId(id);
      setStatus(null);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCheckStatus = async () => {
    if (!challengeId) return;
    try {
      const result = await fetchChallengeStatus(challengeId);
      setStatus(result);
    } catch (err) {
      setError(err.message);
    }
  };

  if (checkingActive) {
    return <p className="challenge-status-loading">불러오는 중...</p>;
  }

  return (
    <div className="challenge-container">
      <h2 className="challenge-title">🧊 냉장고 파먹기 챌린지</h2>

      {!challengeId ? (
        <div className="challenge-start">
          <label>
            기간(일):
            <input
              type="text"
              inputMode="numeric"
              value={daysInput}
              onChange={handleDaysChange}
            />
          </label>
          <button onClick={handleStart}>챌린지 시작</button>
        </div>
      ) : (
        <div className="challenge-status">
          <p>챌린지 진행 중! (id: {challengeId})</p>
          <button onClick={handleCheckStatus}>상태 확인</button>
          {status && (
            <p className={`challenge-badge status-${status.status}`}>
              {status.startDate} ~ {status.endDate} · {status.status}
            </p>
          )}
        </div>
      )}

      {error && <p className="challenge-error">{error}</p>}
    </div>
  );
}