import { useEffect, useRef, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import MyPage from "../pages/MyPage";
import { isLoggedIn, isAdmin, logout } from "../api/authApi";
import NotificationBell from "./NotificationBell";
import "./Nav.css";

const linkClassName = ({ isActive }) => `app-nav-link${isActive ? " active" : ""}`;

export default function Nav() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [loggedIn, setLoggedIn] = useState(isLoggedIn());
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
            {/* TODO: 아래 3개 관리자 메뉴 문구는 인코딩 깨진 걸 임시로 복구한 추측 텍스트입니다.
                관리자 기능 만든 팀원한테 정확한 문구 확인 후 수정해주세요. */}
            <NavLink to="/admin" end className={linkClassName}>
              🛠️ 관리자 대시보드
            </NavLink>
            <NavLink to="/admin/recipes/new" className={linkClassName}>
              📝 레시피 추가
            </NavLink>
            <NavLink to="/admin/reports" className={linkClassName}>
              🚩 신고 관리
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
            </NavLink>
          </>
        )}
      </div>

      {loggedIn && <NotificationBell />}

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
