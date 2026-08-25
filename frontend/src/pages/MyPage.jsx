import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  fetchMyAllergyIngredients,
  registerAllergyIngredient,
  deleteAllergyIngredient,
  fetchAllCookingTools,
  fetchMyTools,
  updateMyTools,
  fetchMyCommunityScraps,
  fetchMyCommunityLikes,
  fetchMyCommunityComments,
} from "../api/userApi";
import { toMediaSrc } from "../api/communityApi";
import { fetchMyScraps, fetchMyMadeRecipes, fetchMyReviewedRecipes } from "../api/socialApi";
import { fetchMyProfile, updateNickname as updateNicknameApi, getCurrentUserId } from "../api/authApi";
import "./MyPage.css";

const TEMP_USER_ID = getCurrentUserId() ?? 1; // 로그인 안 했으면 1(seed 계정)로 폴백

const ACTIVITY_CATEGORIES = [
  { key: "scraps", label: "📌 스크랩한 게시글", fetcher: fetchMyCommunityScraps, type: "community" },
  { key: "likes", label: "❤️ 좋아요한 게시글", fetcher: fetchMyCommunityLikes, type: "community" },
  { key: "comments", label: "💬 댓글단 게시글", fetcher: fetchMyCommunityComments, type: "community" },
];

// 레시피 관련 활동 (나경님 파트: 즐겨찾기/만들어본/평가한 레시피)
const RECIPE_ACTIVITY_CATEGORIES = [
  { key: "favorite-recipes", label: "⭐ 즐겨찾기 레시피", fetcher: fetchMyScraps, type: "recipe" },
  { key: "made-recipes", label: "🍳 만들어본 레시피", fetcher: fetchMyMadeRecipes, type: "recipe" },
  { key: "reviewed-recipes", label: "✍️ 평가한 레시피", fetcher: fetchMyReviewedRecipes, type: "recipe" },
];

export default function MyPage({ onNavigateAway } = {}) {
  const navigate = useNavigate();
  const [allergyIngredients, setAllergyIngredients] = useState([]);
  const [pendingNewIngredients, setPendingNewIngredients] = useState([]);
  const [pendingDeleteIds, setPendingDeleteIds] = useState([]);
  const [newIngredientName, setNewIngredientName] = useState("");
  const [newIngredientType, setNewIngredientType] = useState("알레르기");
  const [allTools, setAllTools] = useState([]);
  const [selectedToolIds, setSelectedToolIds] = useState([]);
  const [savedToolIds, setSavedToolIds] = useState([]);
  const [nickname, setNickname] = useState("");
  const [savedNickname, setSavedNickname] = useState(null); // null = 프로필 조회 실패(비로그인 등) → 닉네임 섹션 자체를 숨김
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [editing, setEditing] = useState(false);
  const [activityScreen, setActivityScreen] = useState(null); // null | "scraps" | "likes" | "comments" | ...
  const [activityPosts, setActivityPosts] = useState([]);
  const [activityLoading, setActivityLoading] = useState(false);
  const [activityError, setActivityError] = useState(null);

  useEffect(() => {
    Promise.all([
      fetchMyAllergyIngredients(TEMP_USER_ID),
      fetchAllCookingTools(),
      fetchMyTools(TEMP_USER_ID),
      fetchMyProfile().catch(() => null), // 비로그인 등으로 실패해도 나머지 섹션은 정상 동작해야 하므로 조용히 null 처리
    ])
      .then(([allergyList, toolList, myTools, profile]) => {
        setAllergyIngredients(allergyList);
        setAllTools(toolList);
        const myToolIds = myTools.map((tool) => tool.toolId);
        setSelectedToolIds(myToolIds);
        setSavedToolIds(myToolIds);
        if (profile) {
          setNickname(profile.nickname);
          setSavedNickname(profile.nickname);
        }
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
  const ingredientsDirty = pendingNewIngredients.length > 0 || pendingDeleteIds.length > 0;
  const nicknameDirty = savedNickname !== null && nickname.trim() !== savedNickname && nickname.trim() !== "";
  const isDirty = toolsDirty || ingredientsDirty || nicknameDirty;

  const handleAddAllergyIngredient = (e) => {
    e.preventDefault();
    const name = newIngredientName.trim();
    if (!name) return;
    setSaved(false);
    setPendingNewIngredients((prev) => [
      ...prev,
      { tempId: `new-${Date.now()}-${prev.length}`, ingredientName: name, type: newIngredientType },
    ]);
    setNewIngredientName("");
  };

  const handleRemoveIngredient = (item) => {
    setSaved(false);
    if (item.tempId) {
      setPendingNewIngredients((prev) => prev.filter((pending) => pending.tempId !== item.tempId));
    } else {
      setPendingDeleteIds((prev) => [...prev, item.id]);
    }
  };

  const toggleTool = (toolId) => {
    setSaved(false);
    setSelectedToolIds((prev) =>
      prev.includes(toolId) ? prev.filter((id) => id !== toolId) : [...prev, toolId]
    );
  };

  const handleSaveAll = async () => {
    setSaving(true);
    try {
      await Promise.all([
        ...pendingDeleteIds.map((id) => deleteAllergyIngredient(TEMP_USER_ID, id)),
        ...pendingNewIngredients.map((item) =>
          registerAllergyIngredient(TEMP_USER_ID, item.ingredientName, item.type)
        ),
      ]);
      if (toolsDirty) {
        await updateMyTools(TEMP_USER_ID, selectedToolIds);
        setSavedToolIds(selectedToolIds);
      }
      if (nicknameDirty) {
        await updateNicknameApi(nickname.trim());
        setSavedNickname(nickname.trim());
      }

      const freshAllergyList = await fetchMyAllergyIngredients(TEMP_USER_ID);
      setAllergyIngredients(freshAllergyList);
      setPendingNewIngredients([]);
      setPendingDeleteIds([]);
      setSaved(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleToggleEdit = () => {
    if (editing) {
      // 수정 모드를 저장 없이 끝내면 저장하지 않은 변경사항은 모두 되돌린다.
      setSelectedToolIds(savedToolIds);
      setPendingNewIngredients([]);
      setPendingDeleteIds([]);
      if (savedNickname !== null) {
        setNickname(savedNickname);
      }
    }
    setSaved(false);
    setEditing((prev) => !prev);
  };

  const openActivityCategory = async (category) => {
    setActivityScreen(category.key);
    setActivityError(null);
    setActivityLoading(true);
    try {
      const posts = await category.fetcher(TEMP_USER_ID);
      setActivityPosts(posts);
    } catch (err) {
      setActivityError(err.message);
    } finally {
      setActivityLoading(false);
    }
  };

  const handleActivityItemClick = (category, item) => {
    onNavigateAway?.();
    if (category.type === "recipe") {
      navigate(`/recipes/${item.recipeId}`);
    } else {
      navigate(`/community/${item.postId}#comments`);
    }
  };

  if (loading) return <p className="mypage-status">불러오는 중...</p>;

  if (activityScreen) {
    const category = [...ACTIVITY_CATEGORIES, ...RECIPE_ACTIVITY_CATEGORIES].find(
      (c) => c.key === activityScreen
    );
    return (
      <div className="mypage-container">
        <div className="mypage-header">
          <button type="button" className="mypage-back-button" onClick={() => setActivityScreen(null)}>
            ← 내 정보
          </button>
        </div>
        <h2 className="mypage-title">{category.label}</h2>

        {activityLoading && <p className="mypage-status">불러오는 중...</p>}
        {activityError && <p className="mypage-status error">{activityError}</p>}
        {!activityLoading && !activityError && activityPosts.length === 0 && (
          <p className="mypage-empty">아직 기록이 없어요.</p>
        )}

        {category.type === "community" && (
          <ul className="mypage-activity-post-list">
            {activityPosts.map((post) => (
              <li
                key={post.postId}
                className="mypage-activity-post-card"
                onClick={() => handleActivityItemClick(category, post)}
              >
                {post.thumbnailUrl && (
                  <div className="mypage-activity-post-thumbnail">
                    <img src={toMediaSrc(post.thumbnailUrl)} alt="" />
                  </div>
                )}
                <div className="mypage-activity-post-body">
                  <div className="mypage-activity-post-title">{post.title}</div>
                  <p className="mypage-activity-post-preview">{post.previewText}</p>
                  <div className="mypage-activity-post-meta">
                    <span>{post.createdAt?.slice(0, 10)}</span>
                    <span>공감 {post.likeCount}</span>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}

        {category.type === "recipe" && (
          <ul className="mypage-activity-post-list">
            {activityPosts.map((recipe) => (
              <li
                key={recipe.recipeId}
                className="mypage-activity-post-card"
                onClick={() => handleActivityItemClick(category, recipe)}
              >
                {recipe.imageUrl && (
                  <div className="mypage-activity-post-thumbnail">
                    <img src={toMediaSrc(recipe.imageUrl)} alt="" />
                  </div>
                )}
                <div className="mypage-activity-post-body">
                  <div className="mypage-activity-post-title">{recipe.recipeName}</div>
                  {category.key === "reviewed-recipes" ? (
                    <>
                      <p className="mypage-activity-post-preview">
                        {"⭐".repeat(recipe.rating)} {recipe.content}
                      </p>
                      <div className="mypage-activity-post-meta">
                        <span>{recipe.createdAt?.slice(0, 10)}</span>
                      </div>
                    </>
                  ) : category.key === "made-recipes" ? (
                    <div className="mypage-activity-post-meta">
                      <span>{recipe.madeAt?.slice(0, 10)}에 만들었어요</span>
                    </div>
                  ) : (
                    <div className="mypage-activity-post-meta">
                      <span>⏱ {recipe.cookingTimeMinutes}분</span>
                      <span>· {recipe.difficulty}</span>
                    </div>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    );
  }

  const visibleIngredients = [
    ...allergyIngredients.filter((item) => !pendingDeleteIds.includes(item.id)),
    ...pendingNewIngredients,
  ];
  const allergyList = visibleIngredients.filter((item) => item.type === "알레르기");
  const avoidList = visibleIngredients.filter((item) => item.type === "기피");

  return (
    <div className="mypage-container">
      <div className="mypage-header">
        <h2 className="mypage-title">🙋 내 정보</h2>
        <div className="mypage-header-actions">
          {editing && isDirty && (
            <button type="button" className="mypage-save-button" onClick={handleSaveAll} disabled={saving}>
              {saving ? "저장 중..." : "저장"}
            </button>
          )}
          {editing && !isDirty && saved && <span className="mypage-save-confirm">저장했어요 ✓</span>}
          <button type="button" className="mypage-edit-toggle" onClick={handleToggleEdit}>
            {editing ? "완료" : "수정"}
          </button>
        </div>
      </div>
      {error && <p className="mypage-status error">{error}</p>}

      {savedNickname !== null && (
        <section className="mypage-section">
          <h3 className="mypage-section-title">닉네임</h3>
          {editing ? (
            <input
              type="text"
              className="mypage-nickname-input"
              value={nickname}
              maxLength={50}
              onChange={(e) => setNickname(e.target.value)}
            />
          ) : (
            <p className="mypage-nickname-view">{savedNickname}</p>
          )}
        </section>
      )}

      <section className="mypage-section">
        <h3 className="mypage-section-title">알레르기 · 기피 재료</h3>

        <div className="mypage-tag-group">
          <h4 className="mypage-tag-group-title">알레르기</h4>
          {allergyList.length === 0 ? (
            <p className="mypage-empty">등록된 알레르기 재료가 없어요.</p>
          ) : (
            <ul className="mypage-tag-list">
              {allergyList.map((item) => (
                <li key={item.tempId ?? item.id} className="mypage-tag danger">
                  {item.ingredientName}
                  {editing && (
                    <button
                      type="button"
                      className="mypage-tag-remove"
                      onClick={() => handleRemoveIngredient(item)}
                      aria-label={`${item.ingredientName} 삭제`}
                    >
                      ×
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="mypage-tag-group">
          <h4 className="mypage-tag-group-title">기피</h4>
          {avoidList.length === 0 ? (
            <p className="mypage-empty">등록된 기피 재료가 없어요.</p>
          ) : (
            <ul className="mypage-tag-list">
              {avoidList.map((item) => (
                <li key={item.tempId ?? item.id} className="mypage-tag">
                  {item.ingredientName}
                  {editing && (
                    <button
                      type="button"
                      className="mypage-tag-remove"
                      onClick={() => handleRemoveIngredient(item)}
                      aria-label={`${item.ingredientName} 삭제`}
                    >
                      ×
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>

        {editing && (
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
        )}
      </section>

      <section className="mypage-section">
        <h3 className="mypage-section-title">보유 조리도구</h3>
        <div className="mypage-tool-grid">
          {allTools.map((tool) =>
            editing ? (
              <button
                type="button"
                key={tool.toolId}
                className={`mypage-tool-chip${selectedToolIds.includes(tool.toolId) ? " active" : ""}`}
                onClick={() => toggleTool(tool.toolId)}
              >
                {tool.toolName}
              </button>
            ) : (
              <span
                key={tool.toolId}
                className={`mypage-tool-chip readonly${savedToolIds.includes(tool.toolId) ? " active" : ""}`}
              >
                {tool.toolName}
              </span>
            )
          )}
        </div>
      </section>

      <section className="mypage-section">
        <h3 className="mypage-section-title">내 활동</h3>

        <p className="mypage-activity-group-title">게시판</p>
        <ul className="mypage-activity-menu">
          {ACTIVITY_CATEGORIES.map((category) => (
            <li key={category.key}>
              <button
                type="button"
                className="mypage-activity-menu-item"
                onClick={() => openActivityCategory(category)}
              >
                <span>{category.label}</span>
                <span className="mypage-activity-menu-arrow">›</span>
              </button>
            </li>
          ))}
        </ul>

        <p className="mypage-activity-group-title">레시피</p>
        <ul className="mypage-activity-menu">
          {RECIPE_ACTIVITY_CATEGORIES.map((category) => (
            <li key={category.key}>
              <button
                type="button"
                className="mypage-activity-menu-item"
                onClick={() => openActivityCategory(category)}
              >
                <span>{category.label}</span>
                <span className="mypage-activity-menu-arrow">›</span>
              </button>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
