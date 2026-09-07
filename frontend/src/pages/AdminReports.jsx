import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  fetchReportedTargets,
  resolveReportByDeleting,
  resolveReportByDismissing,
} from "../api/communityReportApi";
import "./AdminReports.css";

export default function AdminReports() {
  const navigate = useNavigate();
  const [targets, setTargets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actingKey, setActingKey] = useState(null); // 처리 중인 항목(targetType+targetId) 표시용

  const load = () => {
    setLoading(true);
    fetchReportedTargets()
      .then(setTargets)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const keyOf = (target) => `${target.targetType}:${target.targetId}`;

  const handleDelete = async (target) => {
    if (!window.confirm("이 신고 대상을 삭제할까요? 되돌릴 수 없습니다.")) return;
    setActingKey(keyOf(target));
    try {
      await resolveReportByDeleting(target.targetType, target.targetId);
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setActingKey(null);
    }
  };

  const handleDismiss = async (target) => {
    setActingKey(keyOf(target));
    try {
      await resolveReportByDismissing(target.targetType, target.targetId);
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setActingKey(null);
    }
  };

  return (
    <div className="admin-reports-container">
      <h2 className="admin-title">신고 관리</h2>

      {loading && <p className="admin-reports-status">불러오는 중...</p>}
      {error && <p className="admin-reports-status error">{error}</p>}
      {!loading && !error && targets.length === 0 && (
        <p className="admin-reports-status">신고된 게시글/댓글이 없어요.</p>
      )}

      <ul className="admin-reports-list">
        {targets.map((target) => {
          const acting = actingKey === keyOf(target);
          return (
            <li key={keyOf(target)} className="admin-reports-item">
              <div className="admin-reports-item-header">
                <span className={`admin-reports-type-badge ${target.targetType.toLowerCase()}`}>
                  {target.targetType === "POST" ? "게시글" : "댓글"}
                </span>
                {target.hidden && <span className="admin-reports-hidden-badge">자동 숨김됨</span>}
                {target.deleted && <span className="admin-reports-deleted-badge">이미 삭제됨</span>}
                <span className="admin-reports-count">신고 {target.reportCount}건</span>
              </div>

              <p className="admin-reports-preview">{target.preview}</p>
              <p className="admin-reports-meta">
                작성자 {target.authorNickname} · 최근 신고 {target.lastReportedAt?.slice(0, 10)}
              </p>
              <div className="admin-reports-reasons">
                {target.reasons.map((reason, i) => (
                  <span key={i} className="admin-reports-reason-chip">
                    {reason}
                  </span>
                ))}
              </div>

              <div className="admin-reports-actions">
                {target.targetType === "COMMENT" && target.postId && !target.deleted && (
                  <button type="button" onClick={() => navigate(`/community/${target.postId}`)}>
                    게시글에서 보기
                  </button>
                )}
                <button
                  type="button"
                  className="admin-reports-dismiss"
                  disabled={acting}
                  onClick={() => handleDismiss(target)}
                >
                  신고 기각
                </button>
                <button
                  type="button"
                  className="admin-reports-delete"
                  disabled={acting || target.deleted}
                  onClick={() => handleDelete(target)}
                >
                  삭제
                </button>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
