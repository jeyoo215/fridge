import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  fetchMyNotifications,
  fetchUnreadNotificationCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
} from "../api/notificationApi";
import "./NotificationBell.css";

// "방금", "5분 전", "3시간 전"처럼 상대 시간으로 표시
function formatRelativeTime(isoString) {
  const diffMs = Date.now() - new Date(isoString).getTime();
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return "방금";
  if (diffMin < 60) return `${diffMin}분 전`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}시간 전`;
  return `${Math.floor(diffHour / 24)}일 전`;
}

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const wrapperRef = useRef(null);
  const navigate = useNavigate();

  const loadUnreadCount = () => {
    fetchUnreadNotificationCount()
      .then((res) => setUnreadCount(res.count))
      .catch(() => {});
  };

  useEffect(() => {
    loadUnreadCount();
    // 새 댓글 알림이 실시간으로 오는 건 아니라서, 30초마다 새로 확인함
    const intervalId = setInterval(loadUnreadCount, 30_000);
    return () => clearInterval(intervalId);
  }, []);

  // 다른 곳 클릭하면 패널 닫기
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const toggleOpen = () => {
    const next = !open;
    setOpen(next);
    if (next) {
      fetchMyNotifications()
        .then(setNotifications)
        .catch(() => setNotifications([]));
    }
  };

  const handleItemClick = async (notification) => {
    setOpen(false);
    if (!notification.isRead) {
      try {
        await markNotificationAsRead(notification.notificationId);
        setUnreadCount((prev) => Math.max(0, prev - 1));
      } catch {
        // 실패해도 이동은 그대로 진행
      }
    }
    navigate(`/community/${notification.postId}#comments`);
  };

  const handleMarkAllRead = async (e) => {
    e.stopPropagation();
    try {
      await markAllNotificationsAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
    } catch {
      // 실패해도 조용히 무시
    }
  };

  return (
    <div className="notification-bell-wrapper" ref={wrapperRef}>
      <button
        type="button"
        className="notification-bell-button"
        onClick={toggleOpen}
        aria-label="댓글 알림"
      >
        🔔
        {unreadCount > 0 && <span className="notification-bell-badge">{unreadCount}</span>}
      </button>

      {open && (
        <div className="notification-panel">
          <div className="notification-panel-header">
            <span>알림</span>
            {notifications.some((n) => !n.isRead) && (
              <button type="button" className="notification-mark-all-button" onClick={handleMarkAllRead}>
                모두 읽음
              </button>
            )}
          </div>

          {notifications.length === 0 ? (
            <p className="notification-panel-empty">아직 알림이 없어요</p>
          ) : (
            notifications.map((n) => (
              <div
                key={n.notificationId}
                className={`notification-item ${n.isRead ? "" : "unread"}`}
                onClick={() => handleItemClick(n)}
              >
                <p className="notification-item-message">{n.message}</p>
                <span className="notification-item-time">{formatRelativeTime(n.createdAt)}</span>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
