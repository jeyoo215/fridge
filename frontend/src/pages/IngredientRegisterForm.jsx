import { useEffect, useState } from "react";
import { searchIngredients, registerIngredient } from "../api/ingredientApi";
import "./IngredientRegisterForm.css";

const TEMP_USER_ID = 1; // TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체

export default function IngredientRegisterForm({ onRegistered, onCancel }) {
  const [keyword, setKeyword] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [selectedIngredient, setSelectedIngredient] = useState(null); // { ingredientId, ingredientName }
  const [quantity, setQuantity] = useState("");
  const [unit, setUnit] = useState("");
  const [expirationDate, setExpirationDate] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  // 검색어 입력하고 300ms 있다가 검색 (매 글자마다 요청 보내지 않도록)
  useEffect(() => {
    if (selectedIngredient || !keyword) {
      setSearchResults([]);
      return;
    }
    const timer = setTimeout(() => {
      searchIngredients(keyword).then(setSearchResults).catch(() => setSearchResults([]));
    }, 300);
    return () => clearTimeout(timer);
  }, [keyword, selectedIngredient]);

  const handleSelectIngredient = (ingredient) => {
    setSelectedIngredient(ingredient);
    setKeyword(ingredient.ingredientName);
    setSearchResults([]);

    // 재료 기본 보관 가능일수를 알면, 유통기한을 자동으로 미리 채워줌 (사용자가 수정 가능)
    if (ingredient.defaultShelfLifeDays) {
      const d = new Date();
      d.setDate(d.getDate() + ingredient.defaultShelfLifeDays);
      setExpirationDate(d.toISOString().slice(0, 10));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedIngredient) {
      setError("재료를 목록에서 선택해주세요.");
      return;
    }
    if (!quantity || !expirationDate) {
      setError("수량과 유통기한을 입력해주세요.");
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await registerIngredient(TEMP_USER_ID, {
        ingredientId: selectedIngredient.ingredientId,
        quantity: Number(quantity),
        unit,
        expirationDate,
      });
      onRegistered?.();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="ingredient-form" onSubmit={handleSubmit}>
      <h2 className="ingredient-form-title">재료 추가</h2>

      {/* 카메라 인식은 다른 팀원(feature/visionAPICamera)이 실제 Vision API로 작업 중이라
          여기선 수동 입력만 담당함. 나중에 그 브랜치가 merge되면 여기에 카메라 버튼을 연결하면 됨. */}

      <div className="ingredient-form-field">
        <label>재료명</label>
        <input
          type="text"
          placeholder="재료명을 입력하세요 (예: 양파)"
          value={keyword}
          onChange={(e) => {
            setKeyword(e.target.value);
            setSelectedIngredient(null);
          }}
        />
        {searchResults.length > 0 && (
          <ul className="autocomplete-list">
            {searchResults.map((item) => (
              <li key={item.ingredientId} onClick={() => handleSelectIngredient(item)}>
                {item.ingredientName}
                {item.categoryName && <span className="autocomplete-category"> · {item.categoryName}</span>}
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="ingredient-form-row">
        <div className="ingredient-form-field">
          <label>수량</label>
          <input
            type="number"
            min="0"
            step="1"
            placeholder="수량"
            value={quantity}
            onChange={(e) => setQuantity(e.target.value.replace(/[^0-9]/g, ""))}
          />
        </div>
        <div className="ingredient-form-field">
          <label>단위</label>
          <input
            type="text"
            placeholder="개, g, 봉지 등"
            value={unit}
            onChange={(e) => setUnit(e.target.value)}
          />
        </div>
      </div>

      <div className="ingredient-form-field">
        <label>유통기한</label>
        <input
          type="date"
          value={expirationDate}
          onChange={(e) => setExpirationDate(e.target.value)}
        />
      </div>

      {error && <p className="ingredient-form-error">{error}</p>}

      <div className="ingredient-form-actions">
        <button type="button" onClick={onCancel} disabled={submitting}>
          취소
        </button>
        <button type="submit" className="primary" disabled={submitting}>
          {submitting ? "등록 중..." : "등록하기"}
        </button>
      </div>
    </form>
  );
}
