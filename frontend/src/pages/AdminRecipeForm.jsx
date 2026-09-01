import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAccessToken } from "../api/authApi";
import { fetchRecipeCategories } from "../api/recipeApi";
import "./AdminRecipeForm.css";
import { BASE_URL } from "../api/config";

const emptyIngredient = { ingredientId: "", quantity: "", unit: "" };
const emptyStep = { stepOrder: 1, description: "", mediaUrl: "", mediaType: "" };

export default function AdminRecipeForm() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [categoryId, setCategoryId] = useState("");
  const [recipeName, setRecipeName] = useState("");
  const [cookingTimeMinutes, setCookingTimeMinutes] = useState("");
  const [difficulty, setDifficulty] = useState("쉬움");
  const [imageUrl, setImageUrl] = useState("");
  const [ingredients, setIngredients] = useState([{ ...emptyIngredient }]);
  const [steps, setSteps] = useState([{ ...emptyStep }]);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchRecipeCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  const updateIngredient = (index, field, value) => {
    setIngredients((prev) =>
      prev.map((item, i) => (i === index ? { ...item, [field]: value } : item))
    );
  };

  const updateStep = (index, field, value) => {
    setSteps((prev) => prev.map((item, i) => (i === index ? { ...item, [field]: value } : item)));
  };

  const addIngredientRow = () => setIngredients((prev) => [...prev, { ...emptyIngredient }]);
  const removeIngredientRow = (index) =>
    setIngredients((prev) => prev.filter((_, i) => i !== index));

  const addStepRow = () =>
    setSteps((prev) => [...prev, { ...emptyStep, stepOrder: prev.length + 1 }]);
  const removeStepRow = (index) =>
    setSteps((prev) =>
      prev.filter((_, i) => i !== index).map((item, i) => ({ ...item, stepOrder: i + 1 }))
    );

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const payload = {
        categoryId: Number(categoryId),
        recipeName,
        cookingTimeMinutes: Number(cookingTimeMinutes),
        difficulty,
        imageUrl: imageUrl || null,
        source: "관리자",
        ingredients: ingredients
          .filter((item) => item.ingredientId && item.quantity)
          .map((item) => ({
            ingredientId: Number(item.ingredientId),
            quantity: Number(item.quantity),
            unit: item.unit,
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
          {ingredients.map((item, index) => (
            <div className="admin-recipe-form-row" key={index}>
              <input
                type="number"
                placeholder="재료 ID"
                value={item.ingredientId}
                onChange={(e) => updateIngredient(index, "ingredientId", e.target.value)}
              />
              <input
                type="number"
                placeholder="수량"
                value={item.quantity}
                onChange={(e) => updateIngredient(index, "quantity", e.target.value)}
              />
              <input
                placeholder="단위 (개, g 등)"
                value={item.unit}
                onChange={(e) => updateIngredient(index, "unit", e.target.value)}
              />
              {ingredients.length > 1 && (
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