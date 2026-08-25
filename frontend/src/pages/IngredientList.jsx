import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  fetchMyIngredients,
  deleteIngredient,
  updateIngredient,
} from "../api/ingredientApi";
import { fetchFridgeName, updateFridgeName } from "../api/fridgeApi";
import { getCurrentUserId } from "../api/authApi";
import "./IngredientList.css";

const TEMP_USER_ID = getCurrentUserId() ?? 1; // 로그인 안 했으면 1(seed 계정)로 폴백
const SEEN_ALERTS_STORAGE_KEY = `fridge_seen_alerts_user_${TEMP_USER_ID}`;
const ALERT_THRESHOLD_STORAGE_KEY = `fridge_alert_threshold_user_${TEMP_USER_ID}`;
const DEFAULT_ALERT_THRESHOLD = 3;

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

function loadAlertThreshold() {
  const raw = localStorage.getItem(ALERT_THRESHOLD_STORAGE_KEY);
  const parsed = Number(raw);
  // 0 이하이거나 너무 큰 값(30일 넘음)이면 저장된 값이 손상된 걸로 보고 기본값 사용
  if (!raw || Number.isNaN(parsed) || parsed <= 0 || parsed > 30) {
    return DEFAULT_ALERT_THRESHOLD;
  }
  return parsed;
}

function getFreshness(dDay, threshold) {
  if (dDay === null || dDay === undefined) return "normal";
  if (dDay <= 1) return "danger";
  if (dDay <= threshold) return "warning";
  return "normal";
}

function formatDDay(dDay) {
  if (dDay === null || dDay === undefined) return null;
  if (dDay === 0) return "D-DAY";
  return dDay > 0 ? `D-${dDay}` : `D+${Math.abs(dDay)}`;
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
  조미료: "🧂",
  기타: "🧺",
};

const STORAGE_METHOD_LABELS = {
  냉장: "❄️ 냉장 보관",
  냉동: "🧊 냉동 보관",
  실온: "☀️ 실온 보관",
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
  const [alertThreshold, setAlertThreshold] = useState(loadAlertThreshold);
  const [showThresholdSetting, setShowThresholdSetting] = useState(false);

  const [searchKeyword, setSearchKeyword] = useState("");
  const [viewMode, setViewMode] = useState("category"); // "category" | "urgent" | "purchase"
  const [collapsedCategories, setCollapsedCategories] = useState(new Set()); // 접힌 카테고리 이름 모음

  // 일괄 선택 모드
  const [selectMode, setSelectMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [bulkProcessing, setBulkProcessing] = useState(false);

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

  useEffect(() => {
    localStorage.setItem(ALERT_THRESHOLD_STORAGE_KEY, String(alertThreshold));
  }, [alertThreshold]);

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

  // --- 일괄 선택/처리 ---
  const toggleSelectMode = () => {
    setSelectMode((prev) => !prev);
    setSelectedIds(new Set());
    setOpenMenuId(null);
  };

  const toggleSelected = (userIngredientId) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(userIngredientId)) next.delete(userIngredientId);
      else next.add(userIngredientId);
      return next;
    });
  };

  const handleBulkAction = async (actionFn, label) => {
    if (selectedIds.size === 0) return;
    if (!window.confirm(`선택한 ${selectedIds.size}개 재료를 "${label}" 처리할까요?`)) return;

    setBulkProcessing(true);
    setActionError(null);

    const targetIds = Array.from(selectedIds);
    const succeededIds = [];
    const failedNames = [];

    // 하나씩 처리하되, 중간에 실패해도 나머지는 계속 시도 (기존엔 하나 실패하면 전부 멈췄음)
    for (const id of targetIds) {
      try {
        await actionFn(TEMP_USER_ID, id);
        succeededIds.push(id);
      } catch (err) {
        const target = ingredients.find((item) => item.userIngredientId === id);
        failedNames.push(target?.ingredientName || `#${id}`);
      }
    }

    setIngredients((prev) => prev.filter((item) => !succeededIds.includes(item.userIngredientId)));
    setSelectedIds(new Set());
    setSelectMode(false);
    setBulkProcessing(false);

    if (failedNames.length > 0) {
      setActionError(
        `${succeededIds.length}개는 "${label}" 처리됐지만, ${failedNames.length}개는 실패했어요: ${failedNames.join(", ")}`
      );
    }
  };


  // 재료 카드 하나를 렌더링 (카테고리별 뷰/임박순 뷰/구매일순 뷰 다 재사용)
  const renderCard = (item) => {
    const isEditing = editingId === item.userIngredientId;
    const freshness = getFreshness(item.dDay, alertThreshold);
    const dDayLabel = formatDDay(item.dDay);
    const purchaseLabel = formatShortDate(item.purchaseDate);
    const storageLabel = item.storageMethod ? STORAGE_METHOD_LABELS[item.storageMethod] : null;
    const isSelected = selectedIds.has(item.userIngredientId);

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
      <div
        key={item.userIngredientId}
        className={`ingredient-card freshness-${freshness} ${isSelected ? "ingredient-card-selected" : ""}`}
        onClick={selectMode ? () => toggleSelected(item.userIngredientId) : undefined}
      >
        {selectMode && (
          <input
            type="checkbox"
            className="ingredient-select-checkbox"
            checked={isSelected}
            onChange={() => toggleSelected(item.userIngredientId)}
            onClick={(e) => e.stopPropagation()}
          />
        )}
        <span className="freshness-bar" aria-hidden="true" />

        <div className="ingredient-card-main">
          <span className="ingredient-name">{item.ingredientName}</span>
          <span className="ingredient-sub-info">
            {purchaseLabel && <span className="ingredient-purchase-date">구매 {purchaseLabel}</span>}
            {storageLabel && <span className="ingredient-storage-method">{storageLabel}</span>}
          </span>
        </div>

        {dDayLabel && <span className={`dday-badge dday-badge-${freshness}`}>{dDayLabel}</span>}

        {!selectMode && (
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
                <button className="kebab-menu-item" onClick={() => startEdit(item)}>
                  수정
                </button>
                <div className="kebab-menu-divider" />
                <button
                  className="kebab-menu-item kebab-menu-item-danger"
                  onClick={() => handleDelete(item.userIngredientId)}
                >
                  삭제
                </button>
              </div>
            )}
          </div>
        )}
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

  // 임박: 아직 안 지났지만 곧 지나는 것 (0~알림기준일 남음)
  // 조미료는 보통 유통기한이 훨씬 길고 급하게 신경 쓸 대상이 아니라서 알림 대상에서 제외함
  const upcomingIngredients = ingredients.filter(
    (item) =>
      !item.isSeasoning &&
      item.dDay !== null &&
      item.dDay !== undefined &&
      item.dDay >= 0 &&
      item.dDay <= alertThreshold
  );
  // 지난 것: 이미 소비기한이 지난 것 (마이너스) — 이건 조미료도 실제로 상했을 수 있으니 알림 대상 유지
  const expiredIngredients = ingredients.filter(
    (item) => item.dDay !== null && item.dDay !== undefined && item.dDay < 0
  );
  // 알림 종 배지/읽음 처리는 "임박 + 지난 것" 둘 다 대상 (둘 다 관심이 필요하니까)
  const attentionIngredients = [...expiredIngredients, ...upcomingIngredients];
  const unseenAlertCount = attentionIngredients.filter(
    (item) => !seenAlertIds.has(item.userIngredientId)
  ).length;

  const toggleAlerts = () => {
    setShowAlerts((prev) => {
      const next = !prev;
      if (next) {
        setSeenAlertIds((prevSeen) => {
          const merged = new Set(prevSeen);
          attentionIngredients.forEach((item) => merged.add(item.userIngredientId));
          return merged;
        });
      }
      return next;
    });
  };

  const categoryCount = new Set(ingredients.map((i) => i.categoryName || "기타")).size;

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

  // 구매일순 뷰: 최근에 산 것부터
  const purchaseSorted = [...filteredIngredients].sort((a, b) => {
    const aDate = a.purchaseDate ?? "0000-00-00";
    const bDate = b.purchaseDate ?? "0000-00-00";
    return bDate.localeCompare(aDate);
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
                <div className="alert-panel-top">
                  <button
                    type="button"
                    className="alert-threshold-toggle"
                    onClick={() => setShowThresholdSetting((prev) => !prev)}
                  >
                    ⚙️ 알림 기준: D-{alertThreshold}
                  </button>
                </div>

                {showThresholdSetting && (
                  <div className="alert-threshold-setting">
                    <label>며칠 전부터 알려줄까요?</label>
                    <select
                      value={alertThreshold}
                      onChange={(e) => setAlertThreshold(Number(e.target.value))}
                    >
                      {[1, 2, 3, 5, 7, 10].map((d) => (
                        <option key={d} value={d}>
                          D-{d}
                        </option>
                      ))}
                    </select>
                  </div>
                )}

                {attentionIngredients.length === 0 ? (
                  <p className="alert-panel-empty">임박한 재료가 없어요 👍</p>
                ) : (
                  <>
                    {expiredIngredients.length > 0 && (
                      <>
                        <p className="alert-panel-title alert-panel-title-danger">
                          지난 재료 · 바로 처리해주세요
                        </p>
                        {expiredIngredients.map((item) => (
                          <div key={item.userIngredientId} className="alert-panel-item">
                            <span className="alert-panel-dot alert-dot-danger" />
                            <span className="alert-panel-text">
                              {item.ingredientName} 소비기한 {Math.abs(item.dDay)}일 지났어요
                            </span>
                          </div>
                        ))}
                      </>
                    )}
                    {upcomingIngredients.length > 0 && (
                      <>
                        <p className="alert-panel-title">임박 재료</p>
                        {upcomingIngredients.map((item) => (
                          <div key={item.userIngredientId} className="alert-panel-item">
                            <span className={`alert-panel-dot alert-dot-${getFreshness(item.dDay, alertThreshold)}`} />
                            <span className="alert-panel-text">
                              {item.ingredientName} 소비기한{" "}
                              {item.dDay === 0 ? "오늘까지예요" : `${item.dDay}일 남았어요`}
                            </span>
                          </div>
                        ))}
                      </>
                    )}
                  </>
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
              <span className="stat-value">{upcomingIngredients.length}</span>
              <span className="stat-label">임박 재료</span>
            </div>
            <div className={`stat-card ${expiredIngredients.length > 0 ? "stat-card-danger" : "stat-card-good"}`}>
              <span className="stat-value">{expiredIngredients.length}</span>
              <span className="stat-label">지난 재료</span>
            </div>
            <div className="stat-card">
              <span className="stat-value">{categoryCount}</span>
              <span className="stat-label">카테고리</span>
            </div>
          </div>
        </section>

        {/* ---------- 검색 + 보기 전환 + 선택모드 ---------- */}
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
            <button
              className={`view-toggle-button ${viewMode === "purchase" ? "active" : ""}`}
              onClick={() => setViewMode("purchase")}
            >
              구매일순
            </button>
          </div>
          <button
            className={`select-mode-toggle ${selectMode ? "active" : ""}`}
            onClick={toggleSelectMode}
          >
            {selectMode ? "선택 취소" : "여러 개 선택"}
          </button>
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

        {/* ---------- 임박순 뷰 ---------- */}
        {viewMode === "urgent" && urgentSorted.length > 0 && (
          <section className="category-panel">
            <div className="category-grid">{urgentSorted.map(renderCard)}</div>
          </section>
        )}

        {/* ---------- 구매일순 뷰 ---------- */}
        {viewMode === "purchase" && purchaseSorted.length > 0 && (
          <section className="category-panel">
            <div className="category-grid">{purchaseSorted.map(renderCard)}</div>
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

      {/* ---------- 일괄 삭제 하단 바 ---------- */}
      {selectMode && selectedIds.size > 0 && (
        <div className="bulk-action-bar">
          <span>{selectedIds.size}개 선택됨</span>
          <div className="bulk-action-buttons">
            <button
              disabled={bulkProcessing}
              className="bulk-action-danger"
              onClick={() => handleBulkAction(deleteIngredient, "삭제")}
            >
              삭제
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
