import { useState } from "react";
import { NavLink } from "react-router-dom";
import MyPage from "../pages/MyPage";
import "./Nav.css";

const linkClassName = ({ isActive }) => `app-nav-link${isActive ? " active" : ""}`;

export default function Nav() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <nav className="app-nav">
      <div className="app-nav-links">
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
        <NavLink to="/stats" className={linkClassName}>
          📊 통계
        </NavLink>
      </div>

      <button
        type="button"
        className="app-nav-menu-button"
        aria-label="내 정보 열기"
        onClick={() => setMenuOpen(true)}
      >
        ☰
      </button>

      {menuOpen && (
        <div className="app-nav-sheet">
          <div className="app-nav-sheet-header">
            <button
              type="button"
              className="app-nav-sheet-back"
              aria-label="닫기"
              onClick={() => setMenuOpen(false)}
            >
              ←
            </button>
            <span className="app-nav-sheet-title">내 정보</span>
          </div>
          <div className="app-nav-sheet-body">
            <MyPage onNavigateAway={() => setMenuOpen(false)} />
          </div>
        </div>
      )}
    </nav>
  );
}
