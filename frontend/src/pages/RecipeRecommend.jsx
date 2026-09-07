import { useState } from "react";
import RecipeMyIngredientsSection from "./RecipeMyIngredientsSection";
import RecipeComboSection from "./RecipeComboSection";
import RecipeSearchSection from "./RecipeSearchSection";
import "./RecipeRecommend.css";

export default function RecipeRecommend() {
  const [tab, setTab] = useState("recommend"); // "recommend" | "combo" | "search"
  const [searchNotice, setSearchNotice] = useState(null);

  const handleEmptyRecommend = () => {
    setSearchNotice("등록된 재료가 적어서 맞춤 추천이 어려워요. 대신 전체 레시피를 보여드릴게요!");
    setTab("search");
  };

  return (
    <div className="recipe-recommend-container">
      <div className="recipe-recommend-tabs">
        <button className={tab === "recommend" ? "active" : ""} onClick={() => setTab("recommend")}>
          내 재료로 추천
        </button>
        <button className={tab === "combo" ? "active" : ""} onClick={() => setTab("combo")}>
          의외의 조합 추천
        </button>
        <button
          className={tab === "search" ? "active" : ""}
          onClick={() => {
            setTab("search");
            setSearchNotice(null); // 사용자가 직접 탭을 눌러서 온 거면 안내문구는 안 보여줌
          }}
        >
          전체 레시피 검색
        </button>
      </div>

      {tab === "recommend" && <RecipeMyIngredientsSection onEmptyRecommend={handleEmptyRecommend} />}
      {tab === "combo" && <RecipeComboSection />}
      {tab === "search" && <RecipeSearchSection notice={searchNotice} />}
    </div>
  );
}