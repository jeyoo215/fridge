import { useEffect, useState } from "react";
import {
  fetchMyIngredients,
  consumeIngredient,
  discardIngredient,
  deleteIngredient,
  updateIngredient,
} from "../api/ingredientApi";
import { fetchFridgeName, updateFridgeName } from "../api/fridgeApi";
import "./IngredientList.css";

// TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체하기
const TEMP_USER_ID = 1;

function getDDayStyle(dDay) {
  if (dDay === null || dDay === undefined) return "dday-normal";
  if (dDay <= 1) return "dday-danger";   // 유통기한 D-1 이하: 빨강
  if (dDay <= 3) return "dday-warning";  // D-3 이하: 주황
  return "dday-normal";                  // 그 외: 기본
}

export default function IngredientList({ onAddClick }) {
  const [ingredients, setIngredients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openMenuId, setOpenMenuId] = useState(null); // "..." 메뉴가 열려있는 항목 id
  const [editingId, setEditingId] = useState(null);   // 지금 수정 중인 항목 id
  const [editQuantity, setEditQuantity] = useState("");
  const [editExpirationDate, setEditExpirationDate] = useState("");
  const [actionError, setActionError] = useState(null);

  const [fridgeName, setFridgeName] = useState("내 냉장고");
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [titleDraft, setTitleDraft] = useState("");

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
      .catch(() => {}); // 냉장고 이름 로딩 실패는 기본값("내 냉장고")으로 조용히 넘어감
  }, []);

  const toggleMenu = (id) => {
    setOpenMenuId((prev) => (prev === id ? null : id));
  };

  const startEditTitle = () => {
    setTitleDraft(fridgeName);
    setIsEditingTitle(true);
  };

  const cancelEditTitle = () => {
    setIsEditingTitle(false);
  };

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
    setEditExpirationDate(item.expirationDate);
  };

  const cancelEdit = () => {
    setEditingId(null);
  };

  const saveEdit = async (userIngredientId) => {
    try {
      await updateIngredient(TEMP_USER_ID, userIngredientId, {
        quantity: Number(editQuantity),
        expirationDate: editExpirationDate,
      });
      setEditingId(null);
      loadIngredients(); // 유통기한 바뀌면 D-day/정렬 순서도 바뀌니 목록 새로 조회
    } catch (err) {
      setActionError(err.message);
    }
  };

  if (loading) return <p className="ingredient-status">불러오는 중...</p>;
  if (error) return <p className="ingredient-status">{error}</p>;

  return (
    <div className="ingredient-list-container">
      <div className="ingredient-list-header">
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
            <button className="fridge-title-save" onClick={saveTitle}>
              저장
            </button>
            <button className="fridge-title-cancel" onClick={cancelEditTitle}>
              취소
            </button>
          </div>
        ) : (
          <h2 className="ingredient-list-title" onClick={startEditTitle} title="클릭해서 이름 수정">
            {fridgeName}
            <span className="fridge-title-edit-icon">✎</span>
          </h2>
        )}

        <button className="add-ingredient-button" onClick={onAddClick}>
          + 재료 추가
        </button>
      </div>

      {actionError && <p className="ingredient-status ingredient-action-error">{actionError}</p>}

      {ingredients.length === 0 && (
        <p className="ingredient-status">등록된 재료가 없어요. 재료를 추가해보세요!</p>
      )}

      {ingredients.map((item) => {
        const isEditing = editingId === item.userIngredientId;

        if (isEditing) {
          return (
            <div key={item.userIngredientId} className="ingredient-row ingredient-row-editing">
              <span className="ingredient-name">{item.ingredientName}</span>
              <input
                type="number"
                min="0"
                step="1"
                className="edit-input edit-input-quantity"
                value={editQuantity}
                onChange={(e) => setEditQuantity(e.target.value.replace(/[^0-9]/g, ""))}
              />
              <input
                type="date"
                className="edit-input edit-input-date"
                value={editExpirationDate}
                onChange={(e) => setEditExpirationDate(e.target.value)}
              />
              <button className="edit-save-button" onClick={() => saveEdit(item.userIngredientId)}>
                저장
              </button>
              <button className="edit-cancel-button" onClick={cancelEdit}>
                취소
              </button>
            </div>
          );
        }

        return (
          <div
            key={item.userIngredientId}
            className={`ingredient-row ${getDDayStyle(item.dDay)}`}
          >
            <span className="ingredient-name">
              {item.ingredientName}
              {item.dDay !== null && item.dDay !== undefined &&
                ` · D${item.dDay >= 0 ? `-${item.dDay}` : `+${Math.abs(item.dDay)}`}`}
            </span>

            <button
              className="kebab-button"
              onClick={() => toggleMenu(item.userIngredientId)}
              aria-label="더보기 메뉴"
            >
              ⋯
            </button>

            {openMenuId === item.userIngredientId && (
              <div className="kebab-menu">
                <button
                  className="kebab-menu-item"
                  onClick={() => handleConsume(item.userIngredientId)}
                >
                  사용 완료
                </button>
                <button
                  className="kebab-menu-item"
                  onClick={() => handleDiscard(item.userIngredientId)}
                >
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
        );
      })}
    </div>
  );
}
