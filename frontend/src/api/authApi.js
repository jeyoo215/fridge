import { HOST, BASE_URL } from "./config";

export function getAccessToken() {
  return localStorage.getItem("accessToken");
}

export function getRefreshToken() {
  return localStorage.getItem("refreshToken");
}

export function setTokens(accessToken, refreshToken) {
  localStorage.setItem("accessToken", accessToken);
  localStorage.setItem("refreshToken", refreshToken);
}

export function clearTokens() {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
}

export function isLoggedIn() {
  return !!getAccessToken();
}

// accessToken(JWT)의 exp 클레임이 지났는지 확인. 백엔드가 만료된 토큰을 401로 걸러주지 않으므로
// (모든 요청이 permitAll이고 userId가 조용히 null로만 빠짐) 프론트에서 직접 만료 여부를 감시해야 함.
export function isSessionExpired() {
  const token = getAccessToken();
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return Date.now() >= payload.exp * 1000;
  } catch {
    return true;
  }
}

export const KAKAO_LOGIN_URL = `${HOST}/oauth2/authorization/kakao`;

// 이메일 실시간 중복 확인
export async function checkEmailAvailable(email) {
  const response = await fetch(`${BASE_URL}/auth/check-email?email=${encodeURIComponent(email)}`);
  if (!response.ok) {
    throw new Error("이메일 확인 중 오류가 발생했습니다.");
  }
  const { available } = await response.json();
  return available;
}

// 회원가입 이메일 인증 1단계: 인증 코드 발급 요청 (이메일로 실제 발송됨). { expiresInMinutes } 반환
export async function sendSignupCode(email) {
  const response = await fetch(`${BASE_URL}/auth/signup/send-code`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "인증 코드 발급에 실패했습니다.");
  }
  return response.json();
}

// 회원가입 이메일 인증 2단계: 코드 확인
export async function verifySignupCode(email, code) {
  const response = await fetch(`${BASE_URL}/auth/signup/verify-code`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, code }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "인증에 실패했습니다.");
  }
}

// 닉네임은 여기서 받지 않음 — 가입 후 마이페이지에서 직접 설정
export async function signup(email, password, phone) {
  const response = await fetch(`${BASE_URL}/auth/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password, phone }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "Signup failed.");
  }
  return response.json();
}

// 아이디(이메일) 찾기: 회원가입 때 등록한 휴대폰번호가 일치해야 함
export async function findEmail(phone) {
  const response = await fetch(`${BASE_URL}/auth/find-email`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ phone }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "일치하는 회원 정보를 찾을 수 없습니다.");
  }
  const { email } = await response.json();
  return email;
}

// 권한 받기
export function getRole() {
  const token = getAccessToken();
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.role ?? null;
  } catch {
    return null;
  }
}

export function isAdmin() {
  return getRole() === "ADMIN";
}

// 비밀번호 재설정 1단계: 인증 코드 발급 요청 (이메일로 실제 발송됨). { expiresInMinutes } 반환
export async function requestPasswordReset(email) {
  const response = await fetch(`${BASE_URL}/auth/password-reset/request`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "인증 코드 발급에 실패했습니다.");
  }
  return response.json();
}

// 비밀번호 재설정 2단계: 인증 코드 확인만 함 (성공해야 새 비밀번호 입력칸이 열림)
export async function verifyPasswordResetCode(email, code) {
  const response = await fetch(`${BASE_URL}/auth/password-reset/verify-code`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, code }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "인증에 실패했습니다.");
  }
}

// 비밀번호 재설정 3단계: 코드 인증 완료 후 새 비밀번호 적용
export async function confirmPasswordReset(email, code, newPassword) {
  const response = await fetch(`${BASE_URL}/auth/password-reset/confirm`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, code, newPassword }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "비밀번호 재설정에 실패했습니다.");
  }
}

// 로그인한 내 프로필 조회/닉네임 수정 (JWT 인증 필요)
export async function fetchMyProfile() {
  const response = await fetch(`${BASE_URL}/users/me`, {
    headers: { Authorization: `Bearer ${getAccessToken()}` },
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "내 정보를 불러오지 못했습니다.");
  }
  return response.json();
}

export async function updateNickname(nickname) {
  const response = await fetch(`${BASE_URL}/users/me/nickname`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getAccessToken()}`,
    },
    body: JSON.stringify({ nickname }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "닉네임 수정에 실패했습니다.");
  }
}

export async function login(email, password) {
  const response = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "Login failed.");
  }
  const { accessToken, refreshToken } = await response.json();
  setTokens(accessToken, refreshToken);
  return { accessToken, refreshToken };
}

export async function reissue() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) throw new Error("Login required.");

  const response = await fetch(`${BASE_URL}/auth/reissue`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });
  if (!response.ok) {
    clearTokens();
    throw new Error("Session expired. Please log in again.");
  }
  const { accessToken, refreshToken: newRefreshToken } = await response.json();
  setTokens(accessToken, newRefreshToken);
  return accessToken;
}

export async function logout() {
  const accessToken = getAccessToken();
  clearTokens();
  if (!accessToken) return;

  try {
    await fetch(`${BASE_URL}/auth/logout`, {
      method: "POST",
      headers: { Authorization: `Bearer ${accessToken}` },
    });
  } catch {
    // ignore logout failure on server, local tokens already cleared
  }
}

// 액세스 토큰(JWT) 안에 담긴 userId를 꺼내옴. 서버 인증용은 아니고,
// localStorage 키 이름을 사용자별로 구분하는 등 프론트에서만 참고하는 용도.
export function getUserId() {
  const token = getAccessToken();
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.sub;
  } catch {
    return null;
  }
}
