import { useState } from "react";
import RecipeMyIngredientsSection from "./RecipeMyIngredientsSection";
import RecipeComboSection from "./RecipeComboSection";
import RecipeSearchSection from "./RecipeSearchSection";
import "./RecipeRecommend.css";

export default function RecipeRecommend() {
  const [tab, setTab] = useState("recommend"); // "recommend" | "combo" | "search"

  return (
    <div className="recipe-recommend-container">
      <div className="recipe-recommend-tabs">
        <button className={tab === "recommend" ? "active" : ""} onClick={() => setTab("recommend")}>
          내 재료로 추천
        </button>
        <button className={tab === "combo" ? "active" : ""} onClick={() => setTab("combo")}>
          의외의 조합 추천
        </button>
        <button className={tab === "search" ? "active" : ""} onClick={() => setTab("search")}>
          전체 레시피 검색
        </button>
      </div>

      {tab === "recommend" && <RecipeMyIngredientsSection />}
      {tab === "combo" && <RecipeComboSection />}
      {tab === "search" && <RecipeSearchSection />}
    </div>
  );
}