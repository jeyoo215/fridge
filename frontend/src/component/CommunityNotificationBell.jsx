import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { isLoggedIn, isSessionExpired } from "../api/authApi";
import {
  fetchCommunityNotifications,
  fetchCommunityNotificationUnreadCount,
  markCommunityNotificationRead,
  markAllCommunityNotificationsRead,
} from "../api/communityNotificationApi";
import "./CommunityNotificationBell.css";

const POLL_INTERVAL_MS = 60 * 1000;

function describe(notification) {
  const { type, actorNickname, postTitle } = notification;
  return type === "COMMENT_REPLY"
    ? `${actorNickname}님이 "${postTitle}"에서 내 댓글에 답글을 남겼어요`
    : `${actorNickname}님이 "${postTitle}" 게시글에 댓글을 남겼어요`;
}

// 냉장고 목록 화면의 소비기한 알림 종(alert-bell)과 같은 디자인의 커뮤니티 알림 벨.
// 로그인 안 했으면 아무것도 렌더링하지 않는다 (알림 자체가 내 글/댓글에 대한 것이라 로그인 전용).
export default function CommunityNotificationBell() {
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [showPanel, setShowPanel] = useState(false);
  const [loaded, setLoaded] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoggedIn()) return undefined;
    // 커뮤니티 목록/상세는 로그인 없이도 볼 수 있어서 RequireAuth가 없고, 그래서 오래 머물러
    // 있으면 accessToken이 슬라이딩 연장 안 되고 조용히 만료된다. isLoggedIn()은 토큰이
    // "있는지"만 보고 만료 여부는 안 보므로, 만료된 채로 계속 폴링해서 400만 찍히는 걸 막는다.
    const refreshUnreadCount = () => {
      if (isSessionExpired()) return;
      fetchCommunityNotificationUnreadCount().then(setUnreadCount).catch(() => {});
    };
    refreshUnreadCount();
    const timer = setInterval(refreshUnreadCount, POLL_INTERVAL_MS);
    return () => clearInterval(timer);
  }, []);

  const togglePanel = () => {
    const next = !showPanel;
    setShowPanel(next);
    if (next && !loaded) {
      fetchCommunityNotifications()
        .then((list) => {
          setNotifications(list);
          setLoaded(true);
        })
        .catch(() => {});
    }
  };

  const handleItemClick = async (notification) => {
    setShowPanel(false);
    if (!notification.read) {
      setNotifications((prev) =>
        prev.map((n) => (n.notificationId === notification.notificationId ? { ...n, read: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
      markCommunityNotificationRead(notification.notificationId).catch(() => {});
    }
    navigate(`/community/${notification.postId}#comments`);
  };

  const handleMarkAllRead = async () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    setUnreadCount(0);
    try {
      await markAllCommunityNotificationsRead();
    } catch {
      // 실패해도 다음에 벨을 다시 열면 서버 상태로 다시 맞춰지므로 조용히 무시
    }
  };

  if (!isLoggedIn()) return null;

  return (
    <div className="community-alert-bell-wrapper">
      <button
        type="button"
        className="community-alert-bell-button"
        onClick={togglePanel}
        aria-label="커뮤니티 알림"
      >
        🔔
        {unreadCount > 0 && <span className="community-alert-bell-badge">{unreadCount}</span>}
      </button>

      {showPanel && (
        <div className="community-alert-panel">
          <div className="community-alert-panel-top">
            <span className="community-alert-panel-title">알림</span>
            {notifications.some((n) => !n.read) && (
              <button type="button" className="community-alert-mark-all" onClick={handleMarkAllRead}>
                모두 읽음
              </button>
            )}
          </div>

          {notifications.length === 0 ? (
            <p className="community-alert-panel-empty">
              {loaded ? "아직 알림이 없어요" : "불러오는 중..."}
            </p>
          ) : (
            notifications.map((notification) => (
              <button
                type="button"
                key={notification.notificationId}
                className={`community-alert-panel-item${notification.read ? "" : " unread"}`}
                onClick={() => handleItemClick(notification)}
              >
                <span className={`community-alert-panel-dot ${notification.read ? "read" : "unread"}`} />
                <span className="community-alert-panel-text">{describe(notification)}</span>
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}
