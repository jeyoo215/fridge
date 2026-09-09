import { useEffect, useRef, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import MyPage from "../pages/MyPage";
import { isLoggedIn, isSessionExpired, isAdmin, logout } from "../api/authApi";
import { fetchCommunityNotificationUnreadCount } from "../api/communityNotificationApi";
import "./Nav.css";

const linkClassName = ({ isActive }) => `app-nav-link${isActive ? " active" : ""}`;
const COMMUNITY_UNREAD_POLL_MS = 60 * 1000;

export default function Nav() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [loggedIn, setLoggedIn] = useState(isLoggedIn());
  const [hasUnreadCommunity, setHasUnreadCommunity] = useState(false);
  const pushedHistoryRef = useRef(false);
  const navigate = useNavigate();
  const admin = isAdmin();

  useEffect(() => {
    if (!menuOpen) return undefined;
    window.history.pushState({ mypageSheet: true }, "");
    pushedHistoryRef.current = true;
    const handlePopState = () => {
      pushedHistoryRef.current = false;
      setMenuOpen(false);
    };
    window.addEventListener("popstate", handlePopState);
    return () => window.removeEventListener("popstate", handlePopState);
  }, [menuOpen]);

  useEffect(() => {
    const syncLoginState = () => setLoggedIn(isLoggedIn());
    window.addEventListener("storage", syncLoginState);
    window.addEventListener("focus", syncLoginState);
    return () => {
      window.removeEventListener("storage", syncLoginState);
      window.removeEventListener("focus", syncLoginState);
    };
  }, []);

  // 상단 "📝 커뮤니티" 링크에 안 읽은 알림이 있으면 빨간 점을 띄운다
  // (관리자는 이 링크 자체가 없어서 건너뜀).
  useEffect(() => {
    if (!loggedIn || admin) {
      setHasUnreadCommunity(false);
      return undefined;
    }
    const refresh = () => {
      // 커뮤니티 목록/상세처럼 로그인 없이도 볼 수 있는 화면에 오래 머물러 있으면
      // (RequireAuth가 없어서 accessToken이 슬라이딩 연장 안 됨) 토큰이 만료된 채로도
      // isLoggedIn()은 계속 true라서, 만료된 토큰으로 API를 불러 매번 400이 나고 있었다.
      // 진짜 로그인 상태인지(만료 안 됐는지)까지 같이 확인해서 그 경우엔 조용히 건너뛴다.
      if (isSessionExpired()) {
        setHasUnreadCommunity(false);
        return;
      }
      fetchCommunityNotificationUnreadCount()
        .then((count) => setHasUnreadCommunity(count > 0))
        .catch(() => {});
    };
    refresh();
    const timer = setInterval(refresh, COMMUNITY_UNREAD_POLL_MS);
    window.addEventListener("focus", refresh);
    return () => {
      clearInterval(timer);
      window.removeEventListener("focus", refresh);
    };
  }, [loggedIn, admin]);

  const closeSheet = () => {
    if (pushedHistoryRef.current) {
      window.history.back();
    } else {
      setMenuOpen(false);
    }
  };

  const handleLogout = async () => {
    await logout();
    setLoggedIn(false);
    window.location.href = "/";
  };

  return (
    <nav className="app-nav">
      <div className="app-nav-links">
        {admin ? (
          <>
            <NavLink to="/admin" end className={linkClassName}>
              ⚙️ 조합 배치
            </NavLink>
            <NavLink to="/admin/recipes/new" className={linkClassName}>
              📖 레시피 추가
            </NavLink>
            <NavLink to="/admin/reports" className={linkClassName}>
              🚨 신고 관리
            </NavLink>
          </>
        ) : (
          <>
            <NavLink to="/" end className={linkClassName}>
              🥬 냉장고
            </NavLink>
            <NavLink to="/recipes" className={linkClassName}>
              🍳 레시피
            </NavLink>
            <NavLink to="/shopping-list" className={linkClassName}>
              🛒 장보기
            </NavLink>
            <NavLink to="/challenge" className={linkClassName}>
              🏆 챌린지
            </NavLink>
            <NavLink to="/community" className={linkClassName}>
              📝 커뮤니티
              {hasUnreadCommunity && <span className="app-nav-badge-dot" aria-label="읽지 않은 알림 있음" />}
            </NavLink>
          </>
        )}
      </div>

      {loggedIn && (
        <button type="button" className="app-nav-auth-button" onClick={handleLogout}>
          로그아웃
        </button>
      )}

      {!admin && (
        <button
          type="button"
          className="app-nav-menu-button"
          aria-label="내 정보 열기"
          onClick={() => setMenuOpen(true)}
        >
          ☰
        </button>
      )}

      {menuOpen && (
        <div className="app-nav-sheet">
          <div className="app-nav-sheet-header">
            <button type="button" className="app-nav-sheet-back" aria-label="닫기" onClick={closeSheet}>
              ←
            </button>
            <span className="app-nav-sheet-title">내 정보</span>
          </div>
          <div className="app-nav-sheet-body">
            <MyPage onNavigateAway={closeSheet} />
          </div>
        </div>
      )}
    </nav>
  );
}