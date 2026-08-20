import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { setTokens } from "../api/authApi";

export default function OAuthRedirect() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = useState(null);

  useEffect(() => {
    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");

    if (!accessToken || !refreshToken) {
      setError("로그인 처리 중 문제가 발생했습니다.");
      return;
    }

    setTokens(accessToken, refreshToken);
    navigate("/", { replace: true });
  }, [searchParams, navigate]);

  if (error) {
    return (
      <div className="recipe-detail-status">
        <p>{error}</p>
        <button onClick={() => navigate("/login")}>로그인 페이지로</button>
      </div>
    );
  }

  return <p className="recipe-detail-status">로그인 처리 중...</p>;
}