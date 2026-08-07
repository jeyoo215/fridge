import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  fetchMyIngredients,
  consumeIngredient,
  discardIngredient,
  deleteIngredient,
  updateIngredient,
} from "../api/ingredientApi";
import { fetchFridgeName, updateFridgeName } from "../api/fridgeApi";
import "./IngredientList.css";

const TEMP_USER_ID = 1; // TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체
const SEEN_ALERTS_STORAGE_KEY = `fridge_seen_alerts_user_${TEMP_USER_ID}`;

function loadSeenAlertIds() {
  try {
    const raw = localStorage.getItem(SEEN_ALERTS_STORAGE_KEY);
    return raw ? new Set(JSON.parse(raw)) : new Set();
  } catch {
    return new Set();
  }
}

function saveSeenAlertIds(idSet) {
  try {
    localStorage.setItem(SEEN_ALERTS_STORAGE_KEY, JSON.stringify(Array.from(idSet)));
  } catch {
    // 저장 실패해도(용량 초과 등) 앱 동작에는 지장 없게 조용히 무시
  }
}

function getFreshness(dDay) {
  if (dDay === null || dDay === undefined) return "normal";
  if (dDay <= 1) return "danger";
  if (dDay <= 3) return "warning";
  return "normal";
}

function formatDDay(dDay) {
  if (dDay === null || dDay === undefined) return null;
  return dDay >= 0 ? `D-${dDay}` : `D+${Math.abs(dDay)}`;
}

// "2026-08-06" -> "8/6" 처럼 짧게 표시
function formatShortDate(dateStr) {
  if (!dateStr) return null;
  const [, month, day] = dateStr.split("-");
  return `${Number(month)}/${Number(day)}`;
}

const CATEGORY_ICONS = {
  채소: "🥬",
  육류: "🥩",
  수산물: "🐟",
  유제품: "🥛",
  콩가공품: "🧊",
  알류: "🥚",
  과일: "🍎",
  "곡물/가공식품": "🍞",
  기타: "🧺",
};

function groupByCategory(ingredients) {
  const groups = new Map();
  for (const item of ingredients) {
    const key = item.categoryName || "기타";
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push(item);
  }
  return Array.from(groups.entries());
}

export default function IngredientList() {
  const navigate = useNavigate();
  const [ingredients, setIngredients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openMenuId, setOpenMenuId] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [editQuantity, setEditQuantity] = useState("");
  const [editPurchaseDate, setEditPurchaseDate] = useState("");
  const [editExpirationDate, setEditExpirationDate] = useState("");
  const [actionError, setActionError] = useState(null);

  const [fridgeName, setFridgeName] = useState("내 냉장고");
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [titleDraft, setTitleDraft] = useState("");

  const [showAlerts, setShowAlerts] = useState(false);
  const [seenAlertIds, setSeenAlertIds] = useState(loadSeenAlertIds);

  const [searchKeyword, setSearchKeyword] = useState("");
  const [viewMode, setViewMode] = useState("category"); // "category" | "urgent"
  const [collapsedCategories, setCollapsedCategories] = useState(new Set()); // 접힌 카테고리 이름 모음

  const toggleCategoryCollapse = (categoryName) => {
    setCollapsedCategories((prev) => {
      const next = new Set(prev);
      if (next.has(categoryName)) next.delete(categoryName);
      else next.add(categoryName);
      return next;
    });
  };

  useEffect(() => {
    saveSeenAlertIds(seenAlertIds);
  }, [seenAlertIds]);

  const loadIngredients = () => {
    setLoading(true);
    fetchMyIngredients(TEMP_USER_ID)
      .then(setIngredients)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadIngredients();
    fetchFridgeName(TEMP_USER_ID)
      .then(setFridgeName)
      .catch(() => {});
  }, []);

  const toggleMenu = (id) => {
    setOpenMenuId((prev) => (prev === id ? null : id));
  };

  const startEditTitle = () => {
    setTitleDraft(fridgeName);
    setIsEditingTitle(true);
  };

  const cancelEditTitle = () => setIsEditingTitle(false);

  const saveTitle = async () => {
    const trimmed = titleDraft.trim();
    if (!trimmed) {
      setActionError("냉장고 이름을 입력해주세요.");
      return;
    }
    try {
      const saved = await updateFridgeName(TEMP_USER_ID, trimmed);
      setFridgeName(saved);
      setIsEditingTitle(false);
    } catch (err) {
      setActionError(err.message);
    }
  };

  const handleConsume = async (userIngredientId) => {
    setOpenMenuId(null);
    try {
      await consumeIngredient(TEMP_USER_ID, userIngredientId);
      setIngredients((prev) => prev.filter((item) => item.userIngredientId !== userIngredientId));
    } catch (err) {
      setActionError(err.message);
    }
  };

  const handleDiscard = async (userIngredientId) => {
    setOpenMenuId(null);
    try {
      await discardIngredient(TEMP_USER_ID, userIngredientId);
      setIngredients((prev) => prev.filter((item) => item.userIngredientId !== userIngredientId));
    } catch (err) {
      setActionError(err.message);
    }
  };

  const handleDelete = async (userIngredientId) => {
    setOpenMenuId(null);
    if (!window.confirm("이 재료를 삭제할까요? 되돌릴 수 없어요.")) return;
    try {
      await deleteIngredient(TEMP_USER_ID, userIngredientId);
      setIngredients((prev) => prev.filter((item) => item.userIngredientId !== userIngredientId));
    } catch (err) {
      setActionError(err.message);
    }
  };

  const startEdit = (item) => {
    setOpenMenuId(null);
    setEditingId(item.userIngredientId);
    setEditQuantity(item.quantity);
    setEditPurchaseDate(item.purchaseDate || "");
    setEditExpirationDate(item.expirationDate);
  };

  const cancelEdit = () => setEditingId(null);

  const saveEdit = async (userIngredientId) => {
    try {
      await updateIngredient(TEMP_USER_ID, userIngredientId, {
        quantity: Number(editQuantity),
        purchaseDate: editPurchaseDate || null,
        expirationDate: editExpirationDate,
      });
      setEditingId(null);
      loadIngredients();
    } catch (err) {
      setActionError(err.message);
    }
  };

  // 재료 카드 하나를 렌더링 (카테고리별 뷰/임박순 뷰 둘 다에서 재사용)
  const renderCard = (item) => {
    const isEditing = editingId === item.userIngredientId;
    const freshness = getFreshness(item.dDay);
    const dDayLabel = formatDDay(item.dDay);
    const purchaseLabel = formatShortDate(item.purchaseDate);

    if (isEditing) {
      return (
        <div key={item.userIngredientId} className="ingredient-card ingredient-card-editing">
          <span className="ingredient-name">{item.ingredientName}</span>
          <div className="edit-fields">
            <label className="edit-field-group">
              <span className="edit-field-label">수량</span>
              <input
                type="number"
                min="0"
                step="1"
                className="edit-input edit-input-quantity"
                value={editQuantity}
                onChange={(e) => setEditQuantity(e.target.value.replace(/[^0-9]/g, ""))}
              />
            </label>
            <label className="edit-field-group">
              <span className="edit-field-label">구매일</span>
              <input
                type="date"
                className="edit-input edit-input-date"
                value={editPurchaseDate}
                onChange={(e) => setEditPurchaseDate(e.target.value)}
              />
            </label>
            <label className="edit-field-group">
              <span className="edit-field-label">소비기한</span>
              <input
                type="date"
                className="edit-input edit-input-date"
                value={editExpirationDate}
                onChange={(e) => setEditExpirationDate(e.target.value)}
              />
            </label>
          </div>
          <div className="edit-actions">
            <button className="edit-save-button" onClick={() => saveEdit(item.userIngredientId)}>
              저장
            </button>
            <button className="edit-cancel-button" onClick={cancelEdit}>취소</button>
          </div>
        </div>
      );
    }

    return (
      <div key={item.userIngredientId} className={`ingredient-card freshness-${freshness}`}>
        <span className="freshness-bar" aria-hidden="true" />

        <div className="ingredient-card-main">
          <span className="ingredient-name">{item.ingredientName}</span>
          {purchaseLabel && <span className="ingredient-purchase-date">구매 {purchaseLabel}</span>}
        </div>

        {dDayLabel && <span className={`dday-badge dday-badge-${freshness}`}>{dDayLabel}</span>}

        <div className="kebab-wrapper">
          <button
            className="kebab-button"
            onClick={() => toggleMenu(item.userIngredientId)}
            aria-label="더보기 메뉴"
          >
            ⋯
          </button>

          {openMenuId === item.userIngredientId && (
            <div className="kebab-menu">
              <button className="kebab-menu-item" onClick={() => handleConsume(item.userIngredientId)}>
                사용 완료
              </button>
              <button className="kebab-menu-item" onClick={() => handleDiscard(item.userIngredientId)}>
                폐기 (상함)
              </button>
              <div className="kebab-menu-divider" />
              <button className="kebab-menu-item" onClick={() => startEdit(item)}>
                수정
              </button>
              <div className="kebab-menu-divider" />
              <button
                className="kebab-menu-item kebab-menu-item-danger"
                onClick={() => handleDelete(item.userIngredientId)}
              >
                삭제 (잘못 등록함)
              </button>
            </div>
          )}
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="page">
        <p className="ingredient-status">불러오는 중…</p>
      </div>
    );
  }
  if (error) {
    return (
      <div className="page">
        <p className="ingredient-status ingredient-action-error">{error}</p>
      </div>
    );
  }

  const alertIngredients = ingredients.filter(
    (item) => item.dDay !== null && item.dDay !== undefined && item.dDay <= 3
  );
  const unseenAlertCount = alertIngredients.filter(
    (item) => !seenAlertIds.has(item.userIngredientId)
  ).length;

  const toggleAlerts = () => {
    setShowAlerts((prev) => {
      const next = !prev;
      if (next) {
        setSeenAlertIds((prevSeen) => {
          const merged = new Set(prevSeen);
          alertIngredients.forEach((item) => merged.add(item.userIngredientId));
          return merged;
        });
      }
      return next;
    });
  };

  const categoryCount = new Set(ingredients.map((i) => i.categoryName || "기타")).size;
  const expiredCount = ingredients.filter(
    (item) => item.dDay !== null && item.dDay !== undefined && item.dDay < 0
  ).length;

  // 검색어로 필터링 (재료명 부분 일치, 대소문자 무시)
  const trimmedKeyword = searchKeyword.trim().toLowerCase();
  const filteredIngredients = trimmedKeyword
    ? ingredients.filter((item) => item.ingredientName.toLowerCase().includes(trimmedKeyword))
    : ingredients;

  // 임박순 뷰: 카테고리 구분 없이 유통기한 순서 그대로(백엔드가 이미 오름차순으로 줌)
  const urgentSorted = [...filteredIngredients].sort((a, b) => {
    const aDay = a.dDay ?? Infinity;
    const bDay = b.dDay ?? Infinity;
    return aDay - bDay;
  });

  return (
    <div className="page">
      {/* ---------- 상단 네비게이션 바 ---------- */}
      <nav className="site-nav">
        <div className="site-nav-inner">
          <span className="site-logo">🥬 냉장고 파먹기</span>
          <div className="alert-bell-wrapper">
            <button className="alert-bell-button" onClick={toggleAlerts} aria-label="소비기한 임박 알림">
              🔔
              {unseenAlertCount > 0 && <span className="alert-bell-badge">{unseenAlertCount}</span>}
            </button>

            {showAlerts && (
              <div className="alert-panel">
                <p className="alert-panel-title">소비기한 임박 알림</p>
                {alertIngredients.length === 0 ? (
                  <p className="alert-panel-empty">임박한 재료가 없어요 👍</p>
                ) : (
                  alertIngredients.map((item) => (
                    <div key={item.userIngredientId} className="alert-panel-item">
                      <span className={`alert-panel-dot alert-dot-${getFreshness(item.dDay)}`} />
                      <span className="alert-panel-text">
                        {item.ingredientName} 소비기한{" "}
                        {item.dDay < 0
                          ? `${Math.abs(item.dDay)}일 지났어요`
                          : item.dDay === 0
                          ? "오늘까지예요"
                          : `${item.dDay}일 남았어요`}
                      </span>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        </div>
      </nav>

      <div className="page-content">
        {/* ---------- 페이지 헤더 ---------- */}
        <section className="page-header">
          <div className="page-header-top">
            <div>
              {isEditingTitle ? (
                <div className="fridge-title-edit">
                  <input
                    type="text"
                    className="fridge-title-input"
                    value={titleDraft}
                    maxLength={30}
                    autoFocus
                    onChange={(e) => setTitleDraft(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") saveTitle();
                      if (e.key === "Escape") cancelEditTitle();
                    }}
                  />
                  <button className="fridge-title-save" onClick={saveTitle}>저장</button>
                  <button className="fridge-title-cancel" onClick={cancelEditTitle}>취소</button>
                </div>
              ) : (
                <h1 className="page-title" onClick={startEditTitle} title="클릭해서 이름 수정">
                  {fridgeName}
                  <span className="fridge-title-edit-icon">✎</span>
                </h1>
              )}
              <p className="page-subtitle">오늘 냉장고 상태를 확인해보세요.</p>
            </div>

            <button className="add-ingredient-button" onClick={() => navigate("/ingredients/new")}>
              <span className="add-ingredient-plus">+</span> 재료 추가
            </button>
          </div>

          <div className="stats-row">
            <div className="stat-card">
              <span className="stat-value">{ingredients.length}</span>
              <span className="stat-label">보유 재료</span>
            </div>
            <div className="stat-card stat-card-warning">
              <span className="stat-value">{alertIngredients.length}</span>
              <span className="stat-label">임박 재료</span>
            </div>
            <div className={`stat-card ${expiredCount > 0 ? "stat-card-danger" : "stat-card-good"}`}>
              <span className="stat-value">{expiredCount}</span>
              <span className="stat-label">지난 재료</span>
            </div>
            <div className="stat-card">
              <span className="stat-value">{categoryCount}</span>
              <span className="stat-label">카테고리</span>
            </div>
          </div>
        </section>

        {/* ---------- 검색 + 보기 전환 ---------- */}
        <div className="toolbar">
          <input
            type="text"
            className="search-input"
            placeholder="재료 이름으로 검색"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
          />
          <div className="view-toggle">
            <button
              className={`view-toggle-button ${viewMode === "category" ? "active" : ""}`}
              onClick={() => setViewMode("category")}
            >
              카테고리별
            </button>
            <button
              className={`view-toggle-button ${viewMode === "urgent" ? "active" : ""}`}
              onClick={() => setViewMode("urgent")}
            >
              임박순
            </button>
          </div>
        </div>

        {actionError && <p className="ingredient-status ingredient-action-error">{actionError}</p>}

        {ingredients.length === 0 && (
          <div className="empty-state">
            <div className="empty-state-icon" aria-hidden="true">🥬</div>
            <p className="empty-state-title">냉장고가 비어있어요</p>
            <p className="empty-state-subtitle">재료를 추가하고 소비기한을 관리해보세요.</p>
          </div>
        )}

        {ingredients.length > 0 && filteredIngredients.length === 0 && (
          <div className="empty-state">
            <p className="empty-state-title">"{searchKeyword}"에 해당하는 재료가 없어요</p>
          </div>
        )}

        {/* ---------- 임박순 뷰: 카테고리 구분 없는 단일 그리드 ---------- */}
        {viewMode === "urgent" && urgentSorted.length > 0 && (
          <section className="category-panel">
            <div className="category-grid">{urgentSorted.map(renderCard)}</div>
          </section>
        )}

        {/* ---------- 카테고리별 뷰 ---------- */}
        {viewMode === "category" && filteredIngredients.length > 0 && (
          <div className="category-list">
            {groupByCategory(filteredIngredients).map(([categoryName, items]) => {
              const isCollapsed = collapsedCategories.has(categoryName);
              return (
                <section key={categoryName} className="category-panel">
                  <button
                    type="button"
                    className="category-panel-header category-panel-header-button"
                    onClick={() => toggleCategoryCollapse(categoryName)}
                    aria-expanded={!isCollapsed}
                  >
                    <span className="category-icon" aria-hidden="true">
                      {CATEGORY_ICONS[categoryName] || CATEGORY_ICONS["기타"]}
                    </span>
                    <span className="category-name">{categoryName}</span>
                    <span className="category-count">{items.length}</span>
                    <span className={`category-collapse-arrow ${isCollapsed ? "collapsed" : ""}`} aria-hidden="true">
                      ▾
                    </span>
                  </button>
                  {!isCollapsed && (
                    <div className="category-grid">{items.map(renderCard)}</div>
                  )}
                </section>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
