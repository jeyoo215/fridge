import { useEffect, useState } from "react";
import { fetchMyIngredients, consumeIngredient, deleteIngredient } from "../api/ingredientApi";
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
  const [openMenuId, setOpenMenuId] = useState(null); // 지금 "..." 메뉴가 열려있는 항목 id
  const [actionError, setActionError] = useState(null); // 소진/삭제 중 에러 메시지

  const loadIngredients = () => {
    setLoading(true);
    fetchMyIngredients(TEMP_USER_ID)
      .then(setIngredients)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadIngredients();
  }, []);

  const toggleMenu = (id) => {
    setOpenMenuId((prev) => (prev === id ? null : id));
  };

  const handleConsume = async (userIngredientId) => {
    setOpenMenuId(null);
    try {
      await consumeIngredient(TEMP_USER_ID, userIngredientId);
      // 서버에 다시 안 물어보고, 화면에서 바로 그 항목만 지움 (소진된 재료는 목록에 다시 안 나오니까)
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

  if (loading) return <p className="ingredient-status">불러오는 중...</p>;
  if (error) return <p className="ingredient-status">{error}</p>;

  return (
    <div className="ingredient-list-container">
      <div className="ingredient-list-header">
        <h2 className="ingredient-list-title">내 냉장고</h2>
        <button className="add-ingredient-button" onClick={onAddClick}>
          + 재료 추가
        </button>
      </div>

      {actionError && <p className="ingredient-status ingredient-action-error">{actionError}</p>}

      {ingredients.length === 0 && (
        <p className="ingredient-status">등록된 재료가 없어요. 재료를 추가해보세요!</p>
      )}

      {ingredients.map((item) => (
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
                소진 처리
              </button>
              {/* TODO: 수정 기능은 다음에 구현 (수량/유통기한 변경 폼 필요) */}
              <button className="kebab-menu-item" disabled>
                수정
              </button>
              <button
                className="kebab-menu-item kebab-menu-item-danger"
                onClick={() => handleDelete(item.userIngredientId)}
              >
                삭제
              </button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
