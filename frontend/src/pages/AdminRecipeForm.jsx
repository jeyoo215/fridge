import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAccessToken } from "../api/authApi";
import {
  fetchRecipeCategories,
} from "../api/recipeApi";
import {
  searchIngredients,
  createIngredient,
  fetchIngredientCategories,
} from "../api/ingredientApi";
import "./AdminRecipeForm.css";

const BASE_URL = "http://localhost:8080/api/v1";

const emptyIngredientRow = { keyword: "", matched: null, quantity: "", unit: "" };
const emptyStep = { stepOrder: 1, description: "", mediaUrl: "", mediaType: "" };

export default function AdminRecipeForm() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [ingredientCategories, setIngredientCategories] = useState([]);
  const [categoryId, setCategoryId] = useState("");
  const [recipeName, setRecipeName] = useState("");
  const [cookingTimeMinutes, setCookingTimeMinutes] = useState("");
  const [difficulty, setDifficulty] = useState("쉬움");
  const [imageUrl, setImageUrl] = useState("");
  const [ingredientRows, setIngredientRows] = useState([{ ...emptyIngredientRow }]);
  const [steps, setSteps] = useState([{ ...emptyStep }]);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchRecipeCategories().then(setCategories).catch(() => setCategories([]));
    fetchIngredientCategories().then(setIngredientCategories).catch(() => setIngredientCategories([]));
  }, []);

  // 재료 이름 입력 시 검색 (디바운스)
  const handleKeywordChange = (index, keyword) => {
    setIngredientRows((prev) =>
      prev.map((row, i) => (i === index ? { ...row, keyword, matched: null, searchResults: [] } : row))
    );

    if (!keyword.trim()) return;
    setTimeout(async () => {
      const results = await searchIngredients(keyword).catch(() => []);
      setIngredientRows((prev) =>
        prev.map((row, i) => (i === index ? { ...row, searchResults: results } : row))
      );
    }, 300);
  };

  const handleSelectMatched = (index, ingredient) => {
    setIngredientRows((prev) =>
      prev.map((row, i) =>
        i === index
          ? { ...row, matched: ingredient, keyword: ingredient.ingredientName, searchResults: [] }
          : row
      )
    );
  };

  const handleCreateNewIngredient = async (index) => {
    const row = ingredientRows[index];
    const isSeasoning = window.confirm("조미료인가요? (확인=조미료, 취소=일반 재료)");
    let categoryIdForIngredient = null;

    if (!isSeasoning) {
      const catName = window.prompt(
        "카테고리를 입력하세요: " + ingredientCategories.map((c) => c.categoryName).join(", ")
      );
      const found = ingredientCategories.find((c) => c.categoryName === catName);
      if (!found) {
        setError("유효한 카테고리를 입력해주세요.");
        return;
      }
      categoryIdForIngredient = found.categoryId;
    }

    try {
      const created = await createIngredient({
        ingredientName: row.keyword.trim(),
        categoryId: categoryIdForIngredient,
        storageMethod: null,
        isSeasoning,
      });
      handleSelectMatched(index, created);
    } catch (err) {
      setError(err.message);
    }
  };

  const updateIngredientField = (index, field, value) => {
    setIngredientRows((prev) =>
      prev.map((row, i) => (i === index ? { ...row, [field]: value } : row))
    );
  };

  const addIngredientRow = () => setIngredientRows((prev) => [...prev, { ...emptyIngredientRow }]);
  const removeIngredientRow = (index) =>
    setIngredientRows((prev) => prev.filter((_, i) => i !== index));

  const updateStep = (index, field, value) => {
    setSteps((prev) => prev.map((item, i) => (i === index ? { ...item, [field]: value } : item)));
  };
  const addStepRow = () =>
    setSteps((prev) => [...prev, { ...emptyStep, stepOrder: prev.length + 1 }]);
  const removeStepRow = (index) =>
    setSteps((prev) =>
      prev.filter((_, i) => i !== index).map((item, i) => ({ ...item, stepOrder: i + 1 }))
    );

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // 제출 전 필수값 검증 — 서버 400 에러를 안 보고 여기서 먼저 걸러줌
    if (!categoryId || !recipeName.trim() || !cookingTimeMinutes) {
      setError("카테고리, 레시피 이름, 조리 시간은 필수입니다.");
      return;
    }
    const unmatchedRow = ingredientRows.find((row) => row.keyword.trim() && !row.matched);
    if (unmatchedRow) {
      setError("재료를 검색 결과에서 선택하거나 새로 등록해주세요: " + unmatchedRow.keyword);
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        categoryId: Number(categoryId),
        recipeName,
        cookingTimeMinutes: Number(cookingTimeMinutes),
        difficulty,
        imageUrl: imageUrl || null,
        source: "관리자",
        ingredients: ingredientRows
          .filter((row) => row.matched && row.quantity)
          .map((row) => ({
            ingredientId: row.matched.ingredientId,
            quantity: Number(row.quantity),
            unit: row.unit,
          })),
        steps: steps
          .filter((item) => item.description)
          .map((item) => ({
            stepOrder: item.stepOrder,
            description: item.description,
            mediaUrl: item.mediaUrl || null,
            mediaType: item.mediaType || null,
          })),
        toolIds: [],
      };

      const response = await fetch(`${BASE_URL}/admin/recipes`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${getAccessToken()}`,
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        throw new Error(err.message || "레시피 등록에 실패했습니다.");
      }

      navigate("/admin");
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="admin-recipe-form-container">
      <h2 className="admin-recipe-form-title">레시피 추가</h2>

      <form className="admin-recipe-form" onSubmit={handleSubmit}>
        <label>
          카테고리
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
            <option value="">선택하세요</option>
            {categories.map((c) => (
              <option key={c.categoryId} value={c.categoryId}>
                {c.categoryName}
              </option>
            ))}
          </select>
        </label>

        <label>
          레시피 이름
          <input value={recipeName} onChange={(e) => setRecipeName(e.target.value)} required />
        </label>

        <label>
          조리 시간(분)
          <input
            type="number"
            value={cookingTimeMinutes}
            onChange={(e) => setCookingTimeMinutes(e.target.value)}
            required
          />
        </label>

        <label>
          난이도
          <select value={difficulty} onChange={(e) => setDifficulty(e.target.value)}>
            <option value="쉬움">쉬움</option>
            <option value="보통">보통</option>
            <option value="어려움">어려움</option>
          </select>
        </label>

        <label>
          이미지 URL (선택)
          <input value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} />
        </label>

        <fieldset className="admin-recipe-form-fieldset">
          <legend>재료</legend>
          {ingredientRows.map((row, index) => (
            <div className="admin-recipe-form-ingredient-row" key={index}>
              <div className="admin-recipe-form-ingredient-search">
                <input
                  placeholder="재료 이름 검색"
                  value={row.keyword}
                  onChange={(e) => handleKeywordChange(index, e.target.value)}
                />
                {row.matched && <span className="admin-recipe-form-matched-badge">✓ 선택됨</span>}
                {!row.matched && row.searchResults?.length > 0 && (
                  <ul className="admin-recipe-form-search-results">
                    {row.searchResults.map((ing) => (
                      <li key={ing.ingredientId} onClick={() => handleSelectMatched(index, ing)}>
                        {ing.ingredientName}
                      </li>
                    ))}
                  </ul>
                )}
                {!row.matched && row.keyword.trim() && row.searchResults?.length === 0 && (
                  <button
                    type="button"
                    className="admin-recipe-form-create-ingredient"
                    onClick={() => handleCreateNewIngredient(index)}
                  >
                    "{row.keyword}" 새로 등록
                  </button>
                )}
              </div>
              <input
                type="number"
                placeholder="수량"
                value={row.quantity}
                onChange={(e) => updateIngredientField(index, "quantity", e.target.value)}
              />
              <input
                placeholder="단위"
                value={row.unit}
                onChange={(e) => updateIngredientField(index, "unit", e.target.value)}
              />
              {ingredientRows.length > 1 && (
                <button type="button" onClick={() => removeIngredientRow(index)}>
                  삭제
                </button>
              )}
            </div>
          ))}
          <button type="button" className="admin-recipe-form-add" onClick={addIngredientRow}>
            + 재료 추가
          </button>
        </fieldset>

        <fieldset className="admin-recipe-form-fieldset">
          <legend>조리 순서</legend>
          {steps.map((item, index) => (
            <div className="admin-recipe-form-row admin-recipe-form-row-step" key={index}>
              <span className="admin-recipe-form-step-order">{item.stepOrder}</span>
              <textarea
                placeholder="조리 설명"
                value={item.description}
                onChange={(e) => updateStep(index, "description", e.target.value)}
              />
              {steps.length > 1 && (
                <button type="button" onClick={() => removeStepRow(index)}>
                  삭제
                </button>
              )}
            </div>
          ))}
          <button type="button" className="admin-recipe-form-add" onClick={addStepRow}>
            + 순서 추가
          </button>
        </fieldset>

        {error && <p className="admin-recipe-form-error">{error}</p>}

        <button type="submit" className="admin-recipe-form-submit" disabled={submitting}>
          {submitting ? "등록 중..." : "레시피 등록"}
        </button>
      </form>
    </div>
  );
}