import { Navigate } from "react-router-dom";
import { isLoggedIn } from "../api/authApi";

// 로그인 안 된 상태로 보호된 라우트에 직접 접근하면 로그인 페이지로 보냄
export default function RequireAuth({ children }) {
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />;
  }
  return children;
}