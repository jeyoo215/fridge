import { useState } from "react";
import { startChallenge, fetchChallengeStatus } from "../api/challengeApi";
import "./Challenge.css";

const TEMP_USER_ID = 1;

export default function Challenge() {
  const [days, setDays] = useState(7);
  const [challengeId, setChallengeId] = useState(null);
  const [status, setStatus] = useState(null);
  const [error, setError] = useState(null);

  const handleStart = async () => {
    setError(null);
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

  return (
    <div className="challenge-container">
      <h2 className="challenge-title">🧊 냉장고 파먹기 챌린지</h2>

      {!challengeId ? (
        <div className="challenge-start">
          <label>
            기간(일):
            <input
              type="number"
              min="1"
              value={days}
              onChange={(e) => setDays(Number(e.target.value))}
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