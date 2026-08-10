import { useEffect, useState } from "react";
import { startChallenge, fetchChallengeStatus, fetchActiveChallenge } from "../api/challengeApi";
import BadgeSection from "../component/BadgeSection";
import "./Challenge.css";

const TEMP_USER_ID = 1;

export default function Challenge() {
  const [checkingActive, setCheckingActive] = useState(true);
  const [daysInput, setDaysInput] = useState("7");
  const [challengeId, setChallengeId] = useState(null);
  const [status, setStatus] = useState(null);
  const [error, setError] = useState(null);
  const [checking, setChecking] = useState(false); // 상태 확인 버튼 로딩중
  const [lastCheckedAt, setLastCheckedAt] = useState(null); // 마지막 확인 시각

  useEffect(() => {
    fetchActiveChallenge(TEMP_USER_ID)
      .then((active) => {
        if (active) {
          setChallengeId(active.challengeId);
          setStatus(active);
          setLastCheckedAt(new Date());
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
      setLastCheckedAt(null);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCheckStatus = async () => {
    if (!challengeId) return;
    setChecking(true);
    try {
      const result = await fetchChallengeStatus(challengeId);
      setStatus(result);
      setLastCheckedAt(new Date());
    } catch (err) {
      setError(err.message);
    } finally {
      setChecking(false);
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
          <button onClick={handleCheckStatus} disabled={checking}>
            {checking ? "확인 중..." : "상태 확인"}
          </button>
          {status && (
            <>
              <p className={`challenge-badge status-${status.status}`}>
                {status.startDate} ~ {status.endDate} · {status.status}
              </p>
              {lastCheckedAt && (
                <p className="challenge-last-checked">
                  마지막 확인: {lastCheckedAt.toLocaleTimeString()}
                </p>
              )}
            </>
          )}
        </div>
      )}

      {error && <p className="challenge-error">{error}</p>}

      <BadgeSection />
    </div>
  );
}