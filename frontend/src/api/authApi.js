const BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080") + "/api/v1";
const HOST = "http://localhost:8080";

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

// accessToken(JWT)??exp ?�레?�이 지?�는지 ?�인. 백엔?��? 만료???�큰??401�?걸러주�? ?�으므�?
// (모든 ?�청??permitAll?�고 userId가 조용??null로만 빠짐) ?�론?�에??직접 만료 ?��?�?감시?�야 ??
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

// ?�메???�시�?중복 ?�인
export async function checkEmailAvailable(email) {
  const response = await fetch(`${BASE_URL}/auth/check-email?email=${encodeURIComponent(email)}`);
  if (!response.ok) {
    throw new Error("?�메???�인 �??�류가 발생?�습?�다.");
  }
  const { available } = await response.json();
  return available;
}

// ?�원가???�메???�증 1?�계: ?�증 코드 발급 ?�청 (?�메?�로 ?�제 발송??. { expiresInMinutes } 반환
export async function sendSignupCode(email) {
  const response = await fetch(`${BASE_URL}/auth/signup/send-code`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "?�증 코드 발급???�패?�습?�다.");
  }
  return response.json();
}

// ?�원가???�메???�증 2?�계: 코드 ?�인
export async function verifySignupCode(email, code) {
  const response = await fetch(`${BASE_URL}/auth/signup/verify-code`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, code }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "?�증???�패?�습?�다.");
  }
}

// ?�네?��? ?�기??받�? ?�음 ??가????마이?�이지?�서 직접 ?�정
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

// ?�이???�메?? 찾기: ?�원가?????�록???��??�번?��? ?�치?�야 ??
export async function findEmail(phone) {
  const response = await fetch(`${BASE_URL}/auth/find-email`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ phone }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "?�치?�는 ?�원 ?�보�?찾을 ???�습?�다.");
  }
  const { email } = await response.json();
  return email;
}

// 비�?번호 ?�설??1?�계: ?�증 코드 발급 ?�청 (?�메?�로 ?�제 발송??. { expiresInMinutes } 반환
export async function requestPasswordReset(email) {
  const response = await fetch(`${BASE_URL}/auth/password-reset/request`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "?�증 코드 발급???�패?�습?�다.");
  }
  return response.json();
}

// 비�?번호 ?�설??2?�계: ?�증 코드 ?�인�???(?�공?�야 ??비�?번호 ?�력칸이 ?�림)
export async function verifyPasswordResetCode(email, code) {
  const response = await fetch(`${BASE_URL}/auth/password-reset/verify-code`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, code }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "?�증???�패?�습?�다.");
  }
}

// 비�?번호 ?�설??3?�계: 코드 ?�증 ?�료 ????비�?번호 ?�용
export async function confirmPasswordReset(email, code, newPassword) {
  const response = await fetch(`${BASE_URL}/auth/password-reset/confirm`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, code, newPassword }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "비�?번호 ?�설?�에 ?�패?�습?�다.");
  }
}

// 로그?�한 ???�로??조회/?�네???�정 (JWT ?�증 ?�요)
export async function fetchMyProfile() {
  const response = await fetch(`${BASE_URL}/users/me`, {
    headers: { Authorization: `Bearer ${getAccessToken()}` },
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "???�보�?불러?��? 못했?�니??");
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
    throw new Error(err.message || "?�네???�정???�패?�습?�다.");
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

// ?�세???�큰(JWT) ?�에 ?�긴 userId�?꺼내?? ?�버 ?�증?��? ?�니�?
// localStorage ???�름???�용?�별�?구분?�는 ???�론?�에?�만 참고?�는 ?�도.
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
