import { useEffect, useRef, useState } from "react";
import { searchIngredients, registerIngredient, recognizeIngredientImage } from "../api/ingredientApi";
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

  // 카메라 인식 관련 상태 (가연님의 Vision API 연동 엔드포인트 사용)
  const fileInputRef = useRef(null);
  const [recognizing, setRecognizing] = useState(false);
  const [recognizedCandidates, setRecognizedCandidates] = useState([]);
  const [recognizeError, setRecognizeError] = useState(null);

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
    setRecognizedCandidates([]); // 후보 목록에서 골랐으면 그 목록은 닫아버림

    // 재료 기본 보관 가능일수를 알면, 유통기한을 자동으로 미리 채워줌 (사용자가 수정 가능)
    if (ingredient.defaultShelfLifeDays) {
      const d = new Date();
      d.setDate(d.getDate() + ingredient.defaultShelfLifeDays);
      setExpirationDate(d.toISOString().slice(0, 10));
    }
  };

  const handleCameraClick = () => {
    fileInputRef.current?.click();
  };

  const handleImageSelected = async (e) => {
    const file = e.target.files?.[0];
    e.target.value = ""; // 같은 파일 다시 선택해도 onChange 다시 발생하도록 초기화
    if (!file) return;

    setRecognizing(true);
    setRecognizeError(null);
    setRecognizedCandidates([]);
    setSelectedIngredient(null);
    try {
      const candidates = await recognizeIngredientImage(TEMP_USER_ID, file);
      if (candidates.length === 0) {
        setRecognizeError("재료를 인식하지 못했어요. 직접 입력해주세요.");
      } else {
        setRecognizedCandidates(candidates);
      }
    } catch (err) {
      setRecognizeError(err.message);
    } finally {
      setRecognizing(false);
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

      {/* ---------- 카메라 인식 (가연님의 Vision API 연동) ---------- */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        capture="environment"
        style={{ display: "none" }}
        onChange={handleImageSelected}
      />
      <button
        type="button"
        className="camera-button"
        onClick={handleCameraClick}
        disabled={recognizing}
      >
        {recognizing ? "인식 중…" : "📷 카메라로 인식"}
      </button>

      {recognizeError && <p className="ingredient-form-error">{recognizeError}</p>}

      {recognizedCandidates.length > 0 && (
        <div className="recognized-candidates">
          <p className="recognized-candidates-label">이 중에 맞는 게 있나요?</p>
          {recognizedCandidates.map((c) => (
            <button
              type="button"
              key={c.ingredientId}
              className="recognized-candidate-item"
              onClick={() =>
                handleSelectIngredient({ ingredientId: c.ingredientId, ingredientName: c.ingredientName })
              }
            >
              <span>{c.ingredientName}</span>
              <span className="recognized-candidate-score">{Math.round(c.confidenceScore * 100)}%</span>
            </button>
          ))}
        </div>
      )}

      <p className="ingredient-form-divider">또는 직접 입력</p>

      {/* ---------- 수동 입력 ---------- */}
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
