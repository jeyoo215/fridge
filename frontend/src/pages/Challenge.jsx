import { useEffect, useState } from "react";
import { startChallenge, fetchActiveChallenge, abortChallenge } from "../api/challengeApi";
import { searchIngredients } from "../api/ingredientApi";
import BadgeSection from "../component/BadgeSection";
import "./Challenge.css";

const TEMP_USER_ID = 1;

const CHALLENGE_TYPES = [
  { type: "FRIDGE_CLEAN", label: "🥬 냉장고 파먹기", desc: "기간 동안 장을 안 보고 버텨보세요" },
  { type: "TARGET_INGREDIENT", label: "🎯 특정 재료 소진", desc: "고른 재료를 기간 안에 다 써보세요" },
  { type: "HEALTHY_FOOD", label: "🥗 건강식 챌린지", desc: "준비중이에요", disabled: true },
  { type: "BUDGET_LIMIT", label: "💰 장보기 예산 챌린지", desc: "준비중이에요", disabled: true },
];

export default function Challenge() {
  const [loading, setLoading] = useState(true);
  const [challengeId, setChallengeId] = useState(null);
  const [status, setStatus] = useState(null);
  const [aborting, setAborting] = useState(false);
  const [error, setError] = useState(null);

  // 시작 폼 상태
  const [selectedType, setSelectedType] = useState(null);
  const [daysInput, setDaysInput] = useState("7");
  const [keyword, setKeyword] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [selectedIngredients, setSelectedIngredients] = useState([]);
  const [starting, setStarting] = useState(false);

  useEffect(() => {
    fetchActiveChallenge(TEMP_USER_ID)
      .then((active) => {
        if (active) {
          setChallengeId(active.challengeId);
          setStatus(active);
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!keyword) {
      setSearchResults([]);
      return;
    }
    const timer = setTimeout(() => {
      searchIngredients(keyword).then(setSearchResults).catch(() => {});
    }, 300);
    return () => clearTimeout(timer);
  }, [keyword]);

  const toggleIngredient = (ingredient) => {
    setSelectedIngredients((prev) =>
      prev.some((i) => i.ingredientId === ingredient.ingredientId)
        ? prev.filter((i) => i.ingredientId !== ingredient.ingredientId)
        : [...prev, ingredient]
    );
  };

  const handleDaysChange = (e) => setDaysInput(e.target.value.replace(/[^0-9]/g, ""));

  const handleStart = async () => {
    if (!selectedType) return;
    if (selectedType === "TARGET_INGREDIENT" && selectedIngredients.length === 0) {
      setError("소진할 재료를 하나 이상 선택해주세요.");
      return;
    }
    setStarting(true);
    setError(null);
    try {
      const id = await startChallenge(TEMP_USER_ID, {
        days: Number(daysInput),
        type: selectedType,
        targetIngredientIds: selectedIngredients.map((i) => i.ingredientId),
      });
      setChallengeId(id);
      const active = await fetchActiveChallenge(TEMP_USER_ID);
      setStatus(active);
    } catch (err) {
      setError(err.message);
    } finally {
      setStarting(false);
    }
  };

  const handleAbort = async () => {
    const confirmed = window.confirm(
      "정말 챌린지를 중단하시겠어요?\n중단하면 성공 기록에는 포함되지 않고, 중단 기록으로 남아요."
    );
    if (!confirmed) return;

    setAborting(true);
    setError(null);
    try {
      const result = await abortChallenge(challengeId);
      setStatus(result);
      setChallengeId(null); // 중단됐으니 다시 시작 화면으로
    } catch (err) {
      setError(err.message);
    } finally {
      setAborting(false);
    }
  };

  if (loading) return <p className="challenge-status-loading">불러오는 중...</p>;

  return (
    <div className="challenge-container">
      <h2 className="challenge-title">챌린지</h2>

      {!challengeId ? (
        <div className="challenge-start">
          {!selectedType ? (
            <div className="challenge-type-list">
              {CHALLENGE_TYPES.map((c) => (
                <button
                  key={c.type}
                  className="challenge-type-card"
                  disabled={c.disabled}
                  onClick={() => setSelectedType(c.type)}
                >
                  <span className="challenge-type-label">{c.label}</span>
                  <span className="challenge-type-desc">{c.desc}</span>
                </button>
              ))}
            </div>
          ) : (
            <>
              <button className="challenge-type-back" onClick={() => setSelectedType(null)}>
                ← 다른 챌린지 선택
              </button>

              {selectedType === "TARGET_INGREDIENT" && (
                <div className="challenge-ingredient-picker">
                  <input
                    type="text"
                    placeholder="소진할 재료 검색"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                  />
                  {searchResults.length > 0 && (
                    <ul className="challenge-ingredient-results">
                      {searchResults.map((ingredient) => (
                        <li key={ingredient.ingredientId} onClick={() => { toggleIngredient(ingredient); setKeyword(""); }}>
                          {ingredient.ingredientName}
                        </li>
                      ))}
                    </ul>
                  )}
                  {selectedIngredients.length > 0 && (
                    <div className="challenge-ingredient-chips">
                      {selectedIngredients.map((ingredient) => (
                        <span key={ingredient.ingredientId} className="challenge-ingredient-chip">
                          {ingredient.ingredientName}
                          <button onClick={() => toggleIngredient(ingredient)}>✕</button>
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              )}

              <label>
                기간(일):
                <input type="text" inputMode="numeric" value={daysInput} onChange={handleDaysChange} />
              </label>
              <button onClick={handleStart} disabled={starting}>
                {starting ? "시작하는 중..." : "챌린지 시작"}
              </button>
            </>
          )}
        </div>
      ) : (
        <div className="challenge-status">
          <p>챌린지 진행 중! (id: {challengeId})</p>
          {status && (
            <>
              <p className={`challenge-badge status-${status.status}`}>
                {status.startDate} ~ {status.endDate} · {status.status}
              </p>
              {status.type === "TARGET_INGREDIENT" && status.targetIngredientNames?.length > 0 && (
                <p className="challenge-target-names">
                  소진 대상: {status.targetIngredientNames.join(", ")}
                </p>
              )}
            </>
          )}
          <button className="challenge-abort-btn" onClick={handleAbort} disabled={aborting}>
            {aborting ? "중단하는 중..." : "챌린지 중단"}
          </button>
        </div>
      )}

      {error && <p className="challenge-error">{error}</p>}

      <BadgeSection />
    </div>
  );
}