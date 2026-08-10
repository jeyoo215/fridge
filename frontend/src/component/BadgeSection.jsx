import { useEffect, useState } from "react";
import { fetchMyBadges, fetchMyStreak } from "../api/badgeApi";
import "./BadgeSection.css";

const TEMP_USER_ID = 1;

export default function BadgeSection() {
  const [streak, setStreak] = useState(null);
  const [badges, setBadges] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([fetchMyStreak(TEMP_USER_ID), fetchMyBadges(TEMP_USER_ID)])
      .then(([streakData, badgeData]) => {
        setStreak(streakData);
        setBadges(badgeData);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="badge-section-status">불러오는 중...</p>;
  if (error) return <p className="badge-section-status">{error}</p>;

  return (
    <section className="badge-section">
      <div className="badge-streak-card">
        <div className="badge-streak-item">
          <span className="badge-streak-value">{streak.currentStreak}</span>
          <span className="badge-streak-label">연속 성공</span>
        </div>
        <div className="badge-streak-divider" />
        <div className="badge-streak-item">
          <span className="badge-streak-value">{streak.longestStreak}</span>
          <span className="badge-streak-label">최장 기록</span>
        </div>
        <div className="badge-streak-divider" />
        <div className="badge-streak-item">
          <span className="badge-streak-value">{streak.totalSuccessCount}</span>
          <span className="badge-streak-label">누적 성공</span>
        </div>
      </div>

      <h3 className="badge-section-title">획득한 뱃지</h3>
      {badges.length === 0 ? (
        <p className="badge-section-empty">아직 획득한 뱃지가 없어요. 챌린지에 성공해보세요!</p>
      ) : (
        <ul className="badge-list">
          {badges.map((badge) => (
            <li key={badge.badgeId} className="badge-item">
              <span className="badge-item-icon">🏅</span>
              <div className="badge-item-info">
                <span className="badge-item-name">{badge.badgeName}</span>
                <span className="badge-item-desc">{badge.description}</span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}