const BASE_URL = "http://localhost:8080/api/v1";
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

export const KAKAO_LOGIN_URL = `${HOST}/oauth2/authorization/kakao`;

export async function signup(email, password, nickname) {
  const response = await fetch(`${BASE_URL}/auth/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password, nickname }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "회원가입에 실패했습니다.");
  }
  return response.json();
}

export async function login(email, password) {
  const response = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "로그인에 실패했습니다.");
  }
  const { accessToken, refreshToken } = await response.json();
  setTokens(accessToken, refreshToken);
  return { accessToken, refreshToken };
}

export async function reissue() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) throw new Error("로그인이 필요합니다.");

  const response = await fetch(`${BASE_URL}/auth/reissue`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });
  if (!response.ok) {
    clearTokens();
    throw new Error("세션이 만료되었습니다. 다시 로그인해주세요.");
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
    // 서버 로그아웃 실패해도 로컬 토큰은 이미 지웠으니 무시
  }
}