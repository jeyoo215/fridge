import { useEffect, useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { isLoggedIn, isSessionExpired, clearTokens, extendSessionIfActive } from "../api/authApi";

export default function RequireAuth({ children }) {
  const location = useLocation();
  const [expired, setExpired] = useState(false);

  useEffect(() => {
    if (isLoggedIn() && isSessionExpired()) {
      clearTokens();
      setExpired(true);
    } else {
      extendSessionIfActive(); // 페이지 이동(=새로고침, 네비게이션)할 때마다 슬라이딩 연장
    }
  }, [location.pathname]);

  if (expired) {
    return <Navigate to="/login" replace state={{ expired: true }} />;
  }
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />;
  }
  return children;
}