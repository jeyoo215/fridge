import { useEffect, useState } from "react";
import { fetchMyIngredients } from "../api/ingredientApi";
import "./IngredientList.css";

// TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체하기
const TEMP_USER_ID = 1;

function getDDayStyle(dDay) {
  if (dDay <= 1) return "dday-danger";   // 유통기한 D-1 이하: 빨강
  if (dDay <= 3) return "dday-warning";  // D-3 이하: 주황
  return "dday-normal";                  // 그 외: 기본
}

export default function IngredientList() {
  const [ingredients, setIngredients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openMenuId, setOpenMenuId] = useState(null); // 지금 "..." 메뉴가 열려있는 항목 id

  useEffect(() => {
    fetchMyIngredients(TEMP_USER_ID)
      .then(setIngredients)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const toggleMenu = (id) => {
    setOpenMenuId((prev) => (prev === id ? null : id));
  };

  if (loading) return <p className="ingredient-status">불러오는 중...</p>;
  if (error) return <p className="ingredient-status">{error}</p>;

  return (
    <div className="ingredient-list-container">
      <h2 className="ingredient-list-title">내 냉장고</h2>

      {ingredients.length === 0 && (
        <p className="ingredient-status">등록된 재료가 없어요. 재료를 추가해보세요!</p>
      )}

      {ingredients.map((item) => (
        <div
          key={item.userIngredientId}
          className={`ingredient-row ${getDDayStyle(item.dDay)}`}
        >
          <span className="ingredient-name">
            {item.ingredientName} · D{item.dDay >= 0 ? `-${item.dDay}` : `+${Math.abs(item.dDay)}`}
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
              <button className="kebab-menu-item">소진 처리</button>
              <button className="kebab-menu-item">수정</button>
              <button className="kebab-menu-item kebab-menu-item-danger">삭제</button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
