import { NavLink } from "react-router-dom";
import "./Nav.css";

export default function Nav() {
  return (
    <nav className="app-nav">
      <NavLink to="/" end className={({ isActive }) => `app-nav-link${isActive ? " active" : ""}`}>
        🥬 냉장고
      </NavLink>
      <NavLink to="/recipes" className={({ isActive }) => `app-nav-link${isActive ? " active" : ""}`}>
        🍳 레시피
      </NavLink>
      <NavLink to="/challenge" className={({ isActive }) => `app-nav-link${isActive ? " active" : ""}`}>
        🏆 챌린지
      </NavLink>
    </nav>
  );
}