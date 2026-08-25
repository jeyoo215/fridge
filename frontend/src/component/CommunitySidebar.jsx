import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchActiveChallenge } from "../api/challengeApi";
import { BOARD_CONFIGS } from "../pages/communityBoards";
import { getCurrentUserId } from "../api/authApi";
import "./CommunitySidebar.css";

const TEMP_USER_ID = getCurrentUserId() ?? 1; // 로그인 안 했으면 1(seed 계정)로 폴백

// 게시판 전환용 좌측 사이드바. 챌린지 게시판은 지금 진행 중인 챌린지 종류와 다르면 자물쇠 표시만 하고,
// 클릭 자체는 막지 않는다 (들어가면 목록 페이지가 백엔드와 동일한 잠금 안내를 보여줌).
export default function CommunitySidebar({ activeBoardType }) {
  const navigate = useNavigate();
  const [activeChallengeType, setActiveChallengeType] = useState(undefined); // undefined=확인 중, null=없음

  useEffect(() => {
    fetchActiveChallenge(TEMP_USER_ID)
      .then((challenge) => setActiveChallengeType(challenge?.type ?? null))
      .catch(() => setActiveChallengeType(null));
  }, []);

  return (
    <nav className="community-sidebar-sticky">
      <div className="community-sidebar">
        <h3 className="community-sidebar-title">게시판</h3>
        <ul className="community-sidebar-list">
          {BOARD_CONFIGS.map((board) => {
            const locked = board.challengeType != null && activeChallengeType !== board.challengeType;
            return (
              <li key={board.boardType}>
                <button
                  type="button"
                  className={`community-sidebar-item${board.boardType === activeBoardType ? " active" : ""}`}
                  onClick={() => navigate(board.listPath)}
                >
                  {locked && <span className="community-sidebar-lock">🔒</span>}
                  {board.label}
                </button>
              </li>
            );
          })}
        </ul>
      </div>
    </nav>
  );
}
