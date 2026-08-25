import { useState } from "react";
import {
  login,
  signup,
  findEmail,
  requestPasswordReset,
  verifyPasswordResetCode,
  confirmPasswordReset,
  updateNickname,
  checkEmailAvailable,
  sendSignupCode,
  verifySignupCode,
  KAKAO_LOGIN_URL,
} from "../api/authApi";
import "./Login.css";

// mode: "login" | "signup" | "set-nickname" | "find-email" | "reset-request" | "reset-confirm"
export default function Login() {
  const [mode, setMode] = useState("login");

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [phone, setPhone] = useState("");

  // 회원가입 이메일 인증 단계 상태
  const [emailCheckStatus, setEmailCheckStatus] = useState(null); // null | "available" | "taken"
  const [codeSent, setCodeSent] = useState(false);
  const [signupCode, setSignupCode] = useState("");
  const [emailVerified, setEmailVerified] = useState(false);
  const [signupCodeExpiresInMinutes, setSignupCodeExpiresInMinutes] = useState(null);

  const [newNickname, setNewNickname] = useState("");

  const [findPhone, setFindPhone] = useState("");
  const [foundEmail, setFoundEmail] = useState(null);

  const [resetEmail, setResetEmail] = useState("");
  const [resetCode, setResetCode] = useState("");
  const [resetCodeVerified, setResetCodeVerified] = useState(false);
  const [resetCodeExpiresInMinutes, setResetCodeExpiresInMinutes] = useState(null);
  const [resetNewPassword, setResetNewPassword] = useState("");
  const [resetDone, setResetDone] = useState(false);

  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const resetTransientState = () => {
    setError(null);
    setFoundEmail(null);
    setResetDone(false);
    setResetCodeVerified(false);
    setEmailCheckStatus(null);
    setCodeSent(false);
    setSignupCode("");
    setEmailVerified(false);
  };

  const switchMode = (nextMode) => {
    resetTransientState();
    setMode(nextMode);
  };

  const handleCheckEmail = async () => {
    setError(null);
    setEmailCheckStatus(null);
    if (!email.trim()) {
      setError("이메일을 입력해주세요.");
      return;
    }
    setLoading(true);
    try {
      const available = await checkEmailAvailable(email.trim());
      setEmailCheckStatus(available ? "available" : "taken");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSendSignupCode = async () => {
    setError(null);
    if (!email.trim()) {
      setError("이메일을 입력해주세요.");
      return;
    }
    setLoading(true);
    try {
      const result = await sendSignupCode(email.trim());
      setSignupCodeExpiresInMinutes(result.expiresInMinutes ?? null);
      setCodeSent(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleVerifySignupCode = async () => {
    setError(null);
    if (!signupCode.trim()) {
      setError("인증 코드를 입력해주세요.");
      return;
    }
    setLoading(true);
    try {
      await verifySignupCode(email.trim(), signupCode.trim());
      setEmailVerified(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleLoginOrSignup = async (e) => {
    e.preventDefault();
    setError(null);

    if (mode === "signup" && !emailVerified) {
      setError("이메일 인증을 먼저 완료해주세요.");
      return;
    }
    if (mode === "signup" && password !== passwordConfirm) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    setLoading(true);
    try {
      if (mode === "signup") {
        await signup(email, password, phone);
        await login(email, password);
        setError(null);
        setMode("set-nickname"); // 회원가입 직후엔 랜덤 닉네임을 주지 않고, 로그인 처리 후 바로 본인이 짓게 함
      } else {
        await login(email, password);
        window.location.href = "/"; // navigate 대신 새로고침 — App이 다시 마운트되며 로그인 상태 재평가
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSetNickname = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await updateNickname(newNickname.trim());
      window.location.href = "/";
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleFindEmail = async (e) => {
    e.preventDefault();
    setError(null);
    setFoundEmail(null);
    setLoading(true);
    try {
      const result = await findEmail(findPhone.trim());
      setFoundEmail(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRequestReset = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const result = await requestPasswordReset(resetEmail.trim());
      setResetCodeExpiresInMinutes(result.expiresInMinutes ?? null);
      setMode("reset-confirm");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyResetCode = async (e) => {
    e.preventDefault();
    setError(null);
    if (!resetCode.trim()) {
      setError("인증 코드를 입력해주세요.");
      return;
    }
    setLoading(true);
    try {
      await verifyPasswordResetCode(resetEmail.trim(), resetCode.trim());
      setResetCodeVerified(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmReset = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await confirmPasswordReset(resetEmail.trim(), resetCode.trim(), resetNewPassword);
      setResetDone(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (mode === "set-nickname") {
    return (
      <div className="login-container">
        <h2 className="login-title">닉네임을 지어주세요</h2>
        <form className="login-form" onSubmit={handleSetNickname}>
          <p className="login-result">냉장고 앱에서 사용할 닉네임을 정해주세요.</p>
          <input
            type="text"
            placeholder="닉네임"
            value={newNickname}
            onChange={(e) => setNewNickname(e.target.value)}
            maxLength={50}
            required
            autoFocus
          />
          {error && <p className="login-error">{error}</p>}
          <button type="submit" disabled={loading}>
            {loading ? "저장 중..." : "시작하기"}
          </button>
        </form>
      </div>
    );
  }

  if (mode === "find-email") {
    return (
      <div className="login-container">
        <h2 className="login-title">아이디 찾기</h2>
        <form className="login-form" onSubmit={handleFindEmail}>
          <input
            type="tel"
            placeholder="휴대폰 번호 (예: 01012345678)"
            value={findPhone}
            onChange={(e) => setFindPhone(e.target.value)}
            required
          />
          {error && <p className="login-error">{error}</p>}
          {foundEmail && <p className="login-result">가입하신 아이디는 <strong>{foundEmail}</strong> 입니다.</p>}
          <button type="submit" disabled={loading}>
            {loading ? "확인 중..." : "아이디 찾기"}
          </button>
        </form>
        <p className="login-signup-link">
          <button type="button" onClick={() => switchMode("login")}>
            ← 로그인으로 돌아가기
          </button>
        </p>
      </div>
    );
  }

  if (mode === "reset-request" || mode === "reset-confirm") {
    return (
      <div className="login-container">
        <h2 className="login-title">비밀번호 재설정</h2>

        {mode === "reset-request" && (
          <form className="login-form" onSubmit={handleRequestReset}>
            <input
              type="email"
              placeholder="가입한 이메일"
              value={resetEmail}
              onChange={(e) => setResetEmail(e.target.value)}
              required
            />
            {error && <p className="login-error">{error}</p>}
            <button type="submit" disabled={loading}>
              {loading ? "요청 중..." : "인증 코드 받기"}
            </button>
          </form>
        )}

        {mode === "reset-confirm" && !resetDone && (
          <form
            className="login-form"
            onSubmit={resetCodeVerified ? handleConfirmReset : handleVerifyResetCode}
          >
            <p className="login-result">
              {resetEmail}로 인증 코드를 보냈습니다. 메일함을 확인해주세요.
              {resetCodeExpiresInMinutes != null && (
                <>
                  <br />
                  인증코드는 발급 후 {resetCodeExpiresInMinutes}분간 유효합니다.
                </>
              )}
            </p>
            <input
              type="text"
              placeholder="인증 코드 6자리"
              value={resetCode}
              onChange={(e) => setResetCode(e.target.value)}
              maxLength={6}
              required
              disabled={resetCodeVerified}
            />
            {resetCodeVerified && (
              <>
                <p className="login-check-ok">✓ 이메일 인증이 완료됐습니다.</p>
                <input
                  type="password"
                  placeholder="새 비밀번호"
                  value={resetNewPassword}
                  onChange={(e) => setResetNewPassword(e.target.value)}
                  minLength={8}
                  required
                  autoFocus
                />
              </>
            )}
            {error && <p className="login-error">{error}</p>}
            <button type="submit" disabled={loading}>
              {resetCodeVerified
                ? loading ? "변경 중..." : "비밀번호 변경"
                : loading ? "확인 중..." : "인증확인"}
            </button>
          </form>
        )}

        {resetDone && (
          <div className="login-form">
            <p className="login-result">비밀번호가 변경됐어요. 새 비밀번호로 로그인해주세요.</p>
            <button type="button" onClick={() => switchMode("login")}>
              로그인하러 가기
            </button>
          </div>
        )}

        {!resetDone && (
          <p className="login-signup-link">
            <button type="button" onClick={() => switchMode("login")}>
              ← 로그인으로 돌아가기
            </button>
          </p>
        )}
      </div>
    );
  }

  return (
    <div className="login-container">
      <h2 className="login-title">{mode === "login" ? "로그인" : "회원가입"}</h2>

      <form className="login-form" onSubmit={handleLoginOrSignup}>
        {mode === "signup" && (
          <input
            type="tel"
            placeholder="휴대폰 번호 (예: 01012345678)"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            required
          />
        )}

        {mode === "signup" ? (
          <div className="login-inline-row">
            <input
              type="email"
              placeholder="이메일"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                setEmailCheckStatus(null);
              }}
              disabled={emailVerified}
              required
            />
            <button type="button" onClick={handleCheckEmail} disabled={loading || emailVerified}>
              중복확인
            </button>
          </div>
        ) : (
          <input
            type="email"
            placeholder="이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        )}
        {mode === "signup" && emailCheckStatus === "available" && (
          <p className="login-check-ok">✓ 사용 가능한 이메일입니다.</p>
        )}
        {mode === "signup" && emailCheckStatus === "taken" && (
          <p className="login-check-bad">✕ 이미 사용 중인 이메일입니다.</p>
        )}

        {mode === "signup" && !emailVerified && (
          <button type="button" onClick={handleSendSignupCode} disabled={loading}>
            {codeSent ? "인증코드 다시 받기" : "이메일 인증코드 받기"}
          </button>
        )}

        {mode === "signup" && codeSent && !emailVerified && (
          <>
            {signupCodeExpiresInMinutes != null && (
              <p className="login-result">인증코드는 발급 후 {signupCodeExpiresInMinutes}분간 유효합니다.</p>
            )}
            <div className="login-inline-row">
              <input
                type="text"
                placeholder="인증코드 6자리"
                value={signupCode}
                onChange={(e) => setSignupCode(e.target.value)}
                maxLength={6}
              />
              <button type="button" onClick={handleVerifySignupCode} disabled={loading}>
                인증확인
              </button>
            </div>
          </>
        )}

        {mode === "signup" && emailVerified && (
          <p className="login-check-ok">✓ 이메일 인증이 완료됐습니다.</p>
        )}

        {(mode === "login" || emailVerified) && (
          <>
            <input
              type="password"
              placeholder="비밀번호"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={8}
              required
            />
            {mode === "signup" && (
              <input
                type="password"
                placeholder="비밀번호 확인"
                value={passwordConfirm}
                onChange={(e) => setPasswordConfirm(e.target.value)}
                minLength={8}
                required
              />
            )}
          </>
        )}

        {error && <p className="login-error">{error}</p>}

        {(mode === "login" || emailVerified) && (
          <button type="submit" disabled={loading}>
            {loading ? "처리 중..." : mode === "login" ? "로그인" : "회원가입"}
          </button>
        )}
      </form>

      {mode === "login" && (
        <p className="login-links">
          <button type="button" onClick={() => switchMode("find-email")}>
            아이디 찾기
          </button>
          <span>·</span>
          <button type="button" onClick={() => switchMode("reset-request")}>
            비밀번호 재설정
          </button>
        </p>
      )}

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
            <button type="button" onClick={() => switchMode("signup")}>
              회원가입
            </button>
          </>
        ) : (
          <>
            이미 계정이 있으신가요?{" "}
            <button type="button" onClick={() => switchMode("login")}>
              로그인
            </button>
          </>
        )}
      </p>
    </div>
  );
}
