import { useEffect, useState } from "react";
import { fetchMyIngredients, searchIngredients } from "../api/ingredientApi";
import { createFridgeItem, placeFridgeItem } from "../api/fridgeApi";
import "./FridgeAddModal.css";

// 냉장고 꾸미기 화면에 표시할 아이콘용 이모지 (재료 마스터에 없는 것도 자유롭게 고를 수 있게 자주 쓰는 것 위주로 구성)
const EMOJI_OPTIONS = [
  "🥬", "🥕", "🧅", "🥔", "🍅", "🥒", "🥦", "🌽",
  "🍎", "🍌", "🍊", "🍇", "🍓", "🥑", "🍆", "🌶️",
  "🥩", "🍗", "🥓", "🍖", "🐟", "🍤", "🦑", "🥚",
  "🧀", "🥛", "🧈", "🍞", "🍚", "🍜", "🧂", "🥫",
];

// 좌표는 화면 아무 데나 걸치지 않게 살짝 랜덤을 줘서, 여러 개 추가해도 서로 안 겹치게 함
function randomPosition() {
  return {
    posX: 0.15 + Math.random() * 0.7,
    posY: 0.5 + Math.random() * 0.35, // 0.4 밑(냉장 구역)에 기본으로 놓음
  };
}

export default function FridgeAddModal({ onClose, onAdded }) {
  // "existing": 이미 등록해둔 보유재료 중에서 골라 배치 / "new": 재료를 새로 등록하면서 배치
  const [tab, setTab] = useState("existing");
  const [selectedEmoji, setSelectedEmoji] = useState(EMOJI_OPTIONS[0]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  // --- "기존 보유재료" 탭 ---
  const [myIngredients, setMyIngredients] = useState([]);
  const [selectedUserIngredientId, setSelectedUserIngredientId] = useState(null);

  useEffect(() => {
    if (tab !== "existing") return;
    fetchMyIngredients()
      .then(setMyIngredients)
      .catch((e) => setError(e.message));
  }, [tab]);

  // --- "새로 추가" 탭 ---
  const [keyword, setKeyword] = useState("");
  const [candidates, setCandidates] = useState([]);
  const [selectedIngredient, setSelectedIngredient] = useState(null);
  const [quantity, setQuantity] = useState("1");
  const [unit, setUnit] = useState("개");
  const [purchaseDate, setPurchaseDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [expirationDate, setExpirationDate] = useState("");

  useEffect(() => {
    if (tab !== "new" || selectedIngredient || !keyword) {
      setCandidates([]);
      return undefined;
    }
    const timer = setTimeout(() => {
      searchIngredients(keyword)
        .then(setCandidates)
        .catch(() => setCandidates([]));
    }, 300);
    return () => clearTimeout(timer);
  }, [tab, keyword, selectedIngredient]);

  const switchTab = (nextTab) => {
    setTab(nextTab);
    setError("");
  };

  const handleSave = async () => {
    setError("");

    if (tab === "existing") {
      if (!selectedUserIngredientId) {
        setError("배치할 재료를 선택해주세요.");
        return;
      }
      setSaving(true);
      try {
        const { posX, posY } = randomPosition();
        await placeFridgeItem({
          userIngredientId: selectedUserIngredientId,
          imageUrl: selectedEmoji,
          imageType: "SYSTEM",
          posX,
          posY,
          zone: "FRIDGE",
        });
        onAdded?.();
        onClose?.();
      } catch (e) {
        setError(e.message);
      } finally {
        setSaving(false);
      }
      return;
    }

    // tab === "new"
    if (!selectedIngredient) {
      setError("재료를 검색해서 선택해주세요.");
      return;
    }
    if (!expirationDate) {
      setError("소비기한을 입력해주세요.");
      return;
    }
    setSaving(true);
    try {
      const { posX, posY } = randomPosition();
      await createFridgeItem({
        ingredientId: selectedIngredient.ingredientId,
        quantity: Number(quantity) || 1,
        unit,
        purchaseDate,
        expirationDate,
        imageUrl: selectedEmoji,
        imageType: "SYSTEM",
        posX,
        posY,
        zone: "FRIDGE",
      });
      onAdded?.();
      onClose?.();
    } catch (e) {
      setError(e.message);
    } finally {
      setSaving(false);
    }
  };

  const handleSelectCandidate = (ingredient) => {
    setSelectedIngredient(ingredient);
    setKeyword(ingredient.ingredientName);
    setCandidates([]);
  };

  return (
    <div className="fam-overlay" onClick={onClose}>
      <div className="fam-modal" onClick={(e) => e.stopPropagation()}>
        <div className="fam-header">
          <h3>재료 추가</h3>
          <button type="button" className="fam-close" onClick={onClose} aria-label="닫기">
            ×
          </button>
        </div>

        <div className="fam-tabs">
          <button
            type="button"
            className={tab === "existing" ? "active" : ""}
            onClick={() => switchTab("existing")}
          >
            내 냉장고 재료
          </button>
          <button
            type="button"
            className={tab === "new" ? "active" : ""}
            onClick={() => switchTab("new")}
          >
            새로 등록
          </button>
        </div>

        <div className="fam-tab-body">
          {tab === "existing" ? (
            myIngredients.length === 0 ? (
              <p className="fam-todo">아직 등록된 재료가 없어요. "새로 등록" 탭을 이용해주세요.</p>
            ) : (
              <div className="fam-field">
                <label>배치할 재료 선택</label>
                <select
                  value={selectedUserIngredientId ?? ""}
                  onChange={(e) => setSelectedUserIngredientId(Number(e.target.value))}
                >
                  <option value="" disabled>
                    재료를 선택하세요
                  </option>
                  {myIngredients.map((item) => (
                    <option key={item.userIngredientId} value={item.userIngredientId}>
                      {item.ingredientName} (소비기한 {item.expirationDate})
                    </option>
                  ))}
                </select>
              </div>
            )
          ) : (
            <>
              <div className="fam-field">
                <label>재료 이름</label>
                <input
                  type="text"
                  value={keyword}
                  placeholder="예: 상추"
                  onChange={(e) => {
                    setKeyword(e.target.value);
                    setSelectedIngredient(null);
                  }}
                />
                {candidates.length > 0 && (
                  <ul className="fam-candidates">
                    {candidates.map((c) => (
                      <li key={c.ingredientId} onClick={() => handleSelectCandidate(c)}>
                        {c.ingredientName}
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <div className="fam-field">
                <label>수량</label>
                <input
                  type="number"
                  min="1"
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                />
              </div>

              <div className="fam-field">
                <label>단위</label>
                <input type="text" value={unit} onChange={(e) => setUnit(e.target.value)} />
              </div>

              <div className="fam-field">
                <label>구매일</label>
                <input
                  type="date"
                  value={purchaseDate}
                  onChange={(e) => setPurchaseDate(e.target.value)}
                />
              </div>

              <div className="fam-field">
                <label>소비기한</label>
                <input
                  type="date"
                  value={expirationDate}
                  onChange={(e) => setExpirationDate(e.target.value)}
                />
              </div>
            </>
          )}
        </div>

        <div className="fam-field">
          <label>표시할 아이콘 (사진 업로드는 준비중이에요)</label>
          <div className="fam-emoji-grid">
            {EMOJI_OPTIONS.map((emoji) => (
              <button
                key={emoji}
                type="button"
                className={`fam-emoji ${selectedEmoji === emoji ? "selected" : ""}`}
                onClick={() => setSelectedEmoji(emoji)}
              >
                {emoji}
              </button>
            ))}
          </div>
        </div>

        {error && <p className="fam-error">{error}</p>}

        <button type="button" className="fam-save" onClick={handleSave} disabled={saving}>
          {saving ? "저장 중..." : "냉장고에 추가"}
        </button>
      </div>
    </div>
  );
}
