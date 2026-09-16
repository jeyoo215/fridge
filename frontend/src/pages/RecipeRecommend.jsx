import { useState } from "react";
import RecipeRecommendSection from "./RecipeRecommendSection";
import RecipeSearchSection from "./RecipeSearchSection";
import "./RecipeRecommend.css";

export default function RecipeRecommend() {
  const [tab, setTab] = useState("recommend"); // "recommend" | "search"
  const [searchNotice, setSearchNotice] = useState(null);

  const handleEmptyRecommend = () => {
    setSearchNotice("등록된 재료가 적어서 맞춤 추천이 어려워요. 대신 전체 레시피를 보여드릴게요!");
    setTab("search");
  };

  return (
    <div className="recipe-recommend-container">
      <div className="recipe-recommend-tabs">
        <button className={tab === "recommend" ? "active" : ""} onClick={() => setTab("recommend")}>
          레시피 추천
        </button>
        <button
          className={tab === "search" ? "active" : ""}
          onClick={() => {
            setTab("search");
            setSearchNotice(null);
          }}
        >
          전체 레시피 검색
        </button>
      </div>

      {tab === "recommend" && <RecipeRecommendSection onEmptyRecommend={handleEmptyRecommend} />}
      {tab === "search" && <RecipeSearchSection notice={searchNotice} />}
    </div>
  );
}