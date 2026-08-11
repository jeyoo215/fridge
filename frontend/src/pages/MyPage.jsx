import { useEffect, useState } from "react";
import {
  fetchMyAllergyIngredients,
  registerAllergyIngredient,
  deleteAllergyIngredient,
  fetchAllCookingTools,
  fetchMyTools,
  updateMyTools,
} from "../api/userApi";
import "./MyPage.css";

const TEMP_USER_ID = 1; // TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체

export default function MyPage() {
  const [allergyIngredients, setAllergyIngredients] = useState([]);
  const [newIngredientName, setNewIngredientName] = useState("");
  const [newIngredientType, setNewIngredientType] = useState("알레르기");
  const [allTools, setAllTools] = useState([]);
  const [selectedToolIds, setSelectedToolIds] = useState([]);
  const [savedToolIds, setSavedToolIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [savingTools, setSavingTools] = useState(false);
  const [toolsSaved, setToolsSaved] = useState(false);

  useEffect(() => {
    Promise.all([
      fetchMyAllergyIngredients(TEMP_USER_ID),
      fetchAllCookingTools(),
      fetchMyTools(TEMP_USER_ID),
    ])
      .then(([allergyList, toolList, myTools]) => {
        setAllergyIngredients(allergyList);
        setAllTools(toolList);
        const myToolIds = myTools.map((tool) => tool.toolId);
        setSelectedToolIds(myToolIds);
        setSavedToolIds(myToolIds);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const isSameToolSet = (a, b) => {
    if (a.length !== b.length) return false;
    const setA = new Set(a);
    return b.every((id) => setA.has(id));
  };

  const toolsDirty = !isSameToolSet(selectedToolIds, savedToolIds);

  const handleAddAllergyIngredient = async (e) => {
    e.preventDefault();
    const name = newIngredientName.trim();
    if (!name) return;
    try {
      const { id } = await registerAllergyIngredient(TEMP_USER_ID, name, newIngredientType);
      setAllergyIngredients((prev) => [...prev, { id, ingredientName: name, type: newIngredientType }]);
      setNewIngredientName("");
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteAllergyIngredient = async (id) => {
    try {
      await deleteAllergyIngredient(TEMP_USER_ID, id);
      setAllergyIngredients((prev) => prev.filter((item) => item.id !== id));
    } catch (err) {
      setError(err.message);
    }
  };

  const toggleTool = (toolId) => {
    setToolsSaved(false);
    setSelectedToolIds((prev) =>
      prev.includes(toolId) ? prev.filter((id) => id !== toolId) : [...prev, toolId]
    );
  };

  const handleSaveTools = async () => {
    setSavingTools(true);
    try {
      await updateMyTools(TEMP_USER_ID, selectedToolIds);
      setSavedToolIds(selectedToolIds);
      setToolsSaved(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setSavingTools(false);
    }
  };

  if (loading) return <p className="mypage-status">불러오는 중...</p>;

  return (
    <div className="mypage-container">
      <h2 className="mypage-title">🙋 내 정보</h2>
      {error && <p className="mypage-status error">{error}</p>}

      <section className="mypage-section">
        <h3 className="mypage-section-title">알레르기 · 기피 재료</h3>
        {allergyIngredients.length === 0 ? (
          <p className="mypage-empty">등록된 재료가 없어요.</p>
        ) : (
          <ul className="mypage-tag-list">
            {allergyIngredients.map((item) => (
              <li key={item.id} className={`mypage-tag${item.type === "알레르기" ? " danger" : ""}`}>
                <span className="mypage-tag-type">{item.type}</span>
                {item.ingredientName}
                <button
                  type="button"
                  className="mypage-tag-remove"
                  onClick={() => handleDeleteAllergyIngredient(item.id)}
                  aria-label={`${item.ingredientName} 삭제`}
                >
                  ×
                </button>
              </li>
            ))}
          </ul>
        )}
        <form className="mypage-add-form" onSubmit={handleAddAllergyIngredient}>
          <input
            type="text"
            placeholder="재료 이름"
            value={newIngredientName}
            onChange={(e) => setNewIngredientName(e.target.value)}
          />
          <select value={newIngredientType} onChange={(e) => setNewIngredientType(e.target.value)}>
            <option value="알레르기">알레르기</option>
            <option value="기피">기피</option>
          </select>
          <button type="submit">추가</button>
        </form>
      </section>

      <section className="mypage-section">
        <h3 className="mypage-section-title">보유 조리도구</h3>
        <div className="mypage-tool-grid">
          {allTools.map((tool) => (
            <button
              type="button"
              key={tool.toolId}
              className={`mypage-tool-chip${selectedToolIds.includes(tool.toolId) ? " active" : ""}`}
              onClick={() => toggleTool(tool.toolId)}
            >
              {tool.toolName}
            </button>
          ))}
        </div>
        <div className="mypage-tool-actions">
          {toolsDirty && (
            <button type="button" className="mypage-save-button" onClick={handleSaveTools} disabled={savingTools}>
              {savingTools ? "저장 중..." : "저장"}
            </button>
          )}
          {!toolsDirty && toolsSaved && <span className="mypage-save-confirm">저장했어요 ✓</span>}
        </div>
      </section>
    </div>
  );
}
