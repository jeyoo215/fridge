import { useEffect, useState } from "react";
import { startChallenge, fetchActiveChallenge, abortChallenge, fetchChallengeHistory, fetchSuggestedTarget } from "../api/challengeApi";
import { fetchMyIngredients } from "../api/ingredientApi";
import BadgeSection from "../component/BadgeSection";
import "./Challenge.css";

const HISTORY_PAGE_SIZE = 5;

const CHALLENGE_TYPES = [
  { type: "FRIDGE_CLEAN", label: "🥬 냉장고 파먹기", desc: "기간 동안 장을 안 보고 버텨보세요" },
  { type: "TARGET_INGREDIENT", label: "🎯 특정 재료 소진", desc: "고른 재료를 기간 안에 다 써보세요" },
];

const TYPE_LABELS = {
  FRIDGE_CLEAN: "🥬 냉장고 파먹기",
  TARGET_INGREDIENT: "🎯 특정 재료 소진",
};

export default function Challenge() {
  // ── state 선언은 전부 여기 위쪽에 몰아둔다 ──────────────────
  const [loading, setLoading] = useState(true);
  const [challengeId, setChallengeId] = useState(null);
  const [status, setStatus] = useState(null);
  const [history, setHistory] = useState([]);
  const [historyPage, setHistoryPage] = useState(0);
  const [historyTotalPages, setHistoryTotalPages] = useState(0);
  const [aborting, setAborting] = useState(false);
  const [error, setError] = useState(null);

  // 시작 폼 상태
  const [selectedType, setSelectedType] = useState(null);
  const [daysInput, setDaysInput] = useState("7");
  const [keyword, setKeyword] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [selectedIngredients, setSelectedIngredients] = useState([]);
  const [starting, setStarting] = useState(false);

  // 유통기한 임박 추천 배너
  const [suggestion, setSuggestion] = useState(null);

  // 내 보유 재료 (검색 필터링 + "특정 재료 소진" 챌린지 가능 여부 판단용)
  const [myIngredients, setMyIngredients] = useState([]);
  const [hasOwnedIngredients, setHasOwnedIngredients] = useState(true); // 로딩 전 기본값 true(깜빡임 방지)

  // ── effect / 함수들은 그 다음부터 ──────────────────────────

  const loadHistory = (page = 0) => {
    fetchChallengeHistory(page, HISTORY_PAGE_SIZE)
      .then((data) => {
        setHistory(data.content ?? []);
        setHistoryPage(data.page ?? 0);
        setHistoryTotalPages(data.totalPages ?? 0);
      })
      .catch(() => {
        setHistory([]);
        setHistoryTotalPages(0);
      });
  };

  useEffect(() => {
    fetchActiveChallenge()
      .then((active) => {
        if (active && active.status === "진행중") {
          setChallengeId(active.challengeId);
          setStatus(active);
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
    loadHistory(0);
  }, []);

  // 챌린지 시작 화면 진입 시, "특정 재료 소진"을 고를 수 있는지 미리 확인
  useEffect(() => {
    fetchMyIngredients()
      .then((list) => setHasOwnedIngredients(list.length > 0))
      .catch(() => setHasOwnedIngredients(true)); // 조회 실패 시엔 막지 않음(안전한 쪽으로)
  }, []);

  // "특정 재료 소진" 타입을 고르면 내 보유 재료 목록 + 추천 재료를 불러온다
  useEffect(() => {
    if (selectedType === "TARGET_INGREDIENT") {
      fetchMyIngredients().then(setMyIngredients).catch(() => setMyIngredients([]));
      fetchSuggestedTarget().then(setSuggestion).catch(() => setSuggestion(null));
    } else {
      setMyIngredients([]);
      setSuggestion(null);
    }
  }, [selectedType]);

  // 검색창 입력 -> 내 보유 재료 안에서만 필터링 (서버 호출 없음)
  useEffect(() => {
    if (!keyword) {
      setSearchResults([]);
      return;
    }
    const filtered = myIngredients.filter((i) => i.ingredientName.includes(keyword));
    setSearchResults(filtered);
  }, [keyword, myIngredients]);

  const toggleIngredient = (ingredient) => {
    setSelectedIngredients((prev) =>
      prev.some((i) => i.ingredientId === ingredient.ingredientId)
        ? prev.filter((i) => i.ingredientId !== ingredient.ingredientId)
        : [...prev, ingredient]
    );
  };

  const applySuggestion = () => {
    if (!suggestion) return;
    setSelectedIngredients([{ ingredientId: suggestion.ingredientId, ingredientName: suggestion.ingredientName }]);
    setDaysInput(String(suggestion.suggestedDays));
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
      const id = await startChallenge({
        days: Number(daysInput),
        type: selectedType,
        targetIngredientIds: selectedIngredients.map((i) => i.ingredientId),
      });
      setChallengeId(id);
      const active = await fetchActiveChallenge();
      setStatus(active);
      loadHistory(0);
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
      setChallengeId(null);
      loadHistory(0);
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
              {CHALLENGE_TYPES.map((c) => {
                const disabled = c.type === "TARGET_INGREDIENT" && !hasOwnedIngredients;
                return (
                  <button
                    key={c.type}
                    className="challenge-type-card"
                    disabled={disabled}
                    onClick={() => setSelectedType(c.type)}
                  >
                    <span className="challenge-type-label">{c.label}</span>
                    <span className="challenge-type-desc">
                      {disabled ? "냉장고에 재료를 먼저 등록해주세요" : c.desc}
                    </span>
                  </button>
                );
              })}
            </div>
          ) : (
            <>
              <button className="challenge-type-back" onClick={() => setSelectedType(null)}>
                ← 다른 챌린지 선택
              </button>

              {selectedType === "TARGET_INGREDIENT" && (
                <div className="challenge-ingredient-picker">
                  <p className="challenge-hint">
                    💡 선택한 재료를 다 쓰면 기간이 남아있어도 자동으로 성공 처리돼요!
                  </p>

                  {suggestion && (
                    <div className="challenge-suggestion-banner">
                      <p>
                        🔥 <strong>{suggestion.ingredientName}</strong>의 유통기한이 {suggestion.expirationDate}까지예요
                        ({suggestion.quantity}{suggestion.unit} 보유중). 이 재료를 소진하는 걸 추천해요!
                      </p>
                      <button type="button" onClick={applySuggestion}>추천 재료로 설정</button>
                    </div>
                  )}

                  <input
                    type="text"
                    placeholder="소진할 재료 검색 (내 보유 재료 중에서)"
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

      {history?.length > 0 && (
        <section className="challenge-history">
          <h3 className="challenge-history-title">지난 챌린지 기록</h3>
          <ul className="challenge-history-list">
            {history.map((h) => (
              <li key={h.challengeId} className="challenge-history-item">
                <span className="challenge-history-type">{TYPE_LABELS[h.type] || h.type}</span>
                <span className="challenge-history-period">{h.startDate} ~ {h.endDate}</span>
                <span className={`challenge-history-status status-${h.status}`}>{h.status}</span>
              </li>
            ))}
          </ul>

          {historyTotalPages > 1 && (
            <div className="challenge-history-pagination">
              <button
                type="button"
                disabled={historyPage === 0}
                onClick={() => loadHistory(historyPage - 1)}
              >
                이전
              </button>
              {Array.from({ length: historyTotalPages }, (_, i) => i).map((p) => (
                <button
                  type="button"
                  key={p}
                  className={p === historyPage ? "active" : ""}
                  onClick={() => loadHistory(p)}
                >
                  {p + 1}
                </button>
              ))}
              <button
                type="button"
                disabled={historyPage >= historyTotalPages - 1}
                onClick={() => loadHistory(historyPage + 1)}
              >
                다음
              </button>
            </div>
          )}
        </section>
      )}

      <BadgeSection />
    </div>
  );
}