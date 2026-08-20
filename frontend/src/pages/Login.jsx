import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login, signup, KAKAO_LOGIN_URL } from "../api/authApi";
import "./Login.css";

export default function Login() {
  const navigate = useNavigate();
  const [mode, setMode] = useState("login"); // "login" | "signup"
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      if (mode === "signup") {
        await signup(email, password, nickname);
        await login(email, password);
      } else {
        await login(email, password);
      }
      navigate("/");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <h2 className="login-title">{mode === "login" ? "로그인" : "회원가입"}</h2>

      <form className="login-form" onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        {mode === "signup" && (
          <input
            type="text"
            placeholder="닉네임"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            required
          />
        )}
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          minLength={8}
          required
        />
        {error && <p className="login-error">{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? "처리 중..." : mode === "login" ? "로그인" : "회원가입"}
        </button>
      </form>

      <div className="login-divider">또는</div>

      <button
        className="login-kakao-button"
        onClick={() => (window.location.href = KAKAO_LOGIN_URL)}
      >
        카카오로 로그인
      </button>

      <p className="login-signup-link">
        {mode === "login" ? (
          <>
            계정이 없으신가요?{" "}
            <button type="button" onClick={() => setMode("signup")}>
              회원가입
            </button>
          </>
        ) : (
          <>
            이미 계정이 있으신가요?{" "}
            <button type="button" onClick={() => setMode("login")}>
              로그인
            </button>
          </>
        )}
      </p>
    </div>
  );
}