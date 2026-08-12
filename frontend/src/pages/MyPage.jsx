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
import "./MyPage.css";

const TEMP_USER_ID = 1; // TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체

const ACTIVITY_CATEGORIES = [
  { key: "scraps", label: "📌 스크랩한 게시글", fetcher: fetchMyCommunityScraps },
  { key: "likes", label: "❤️ 좋아요한 게시글", fetcher: fetchMyCommunityLikes },
  { key: "comments", label: "💬 댓글단 게시글", fetcher: fetchMyCommunityComments },
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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [editing, setEditing] = useState(false);
  const [activityScreen, setActivityScreen] = useState(null); // null | "menu" | "scraps" | "likes" | "comments"
  const [activityPosts, setActivityPosts] = useState([]);
  const [activityLoading, setActivityLoading] = useState(false);
  const [activityError, setActivityError] = useState(null);

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
  const ingredientsDirty = pendingNewIngredients.length > 0 || pendingDeleteIds.length > 0;
  const isDirty = toolsDirty || ingredientsDirty;

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

  const handleActivityPostClick = (postId) => {
    onNavigateAway?.();
    navigate(`/community/${postId}#comments`);
  };

  if (loading) return <p className="mypage-status">불러오는 중...</p>;

  if (activityScreen === "menu") {
    return (
      <div className="mypage-container">
        <div className="mypage-header">
          <button type="button" className="mypage-back-button" onClick={() => setActivityScreen(null)}>
            ← 내 정보
          </button>
        </div>
        <h2 className="mypage-title">내 활동</h2>
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
      </div>
    );
  }

  if (activityScreen) {
    const category = ACTIVITY_CATEGORIES.find((c) => c.key === activityScreen);
    return (
      <div className="mypage-container">
        <div className="mypage-header">
          <button type="button" className="mypage-back-button" onClick={() => setActivityScreen("menu")}>
            ← 내 활동
          </button>
        </div>
        <h2 className="mypage-title">{category.label}</h2>

        {activityLoading && <p className="mypage-status">불러오는 중...</p>}
        {activityError && <p className="mypage-status error">{activityError}</p>}
        {!activityLoading && !activityError && activityPosts.length === 0 && (
          <p className="mypage-empty">아직 기록이 없어요.</p>
        )}

        <ul className="mypage-activity-post-list">
          {activityPosts.map((post) => (
            <li
              key={post.postId}
              className="mypage-activity-post-card"
              onClick={() => handleActivityPostClick(post.postId)}
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
        <button type="button" className="mypage-activity-entry" onClick={() => setActivityScreen("menu")}>
          <span>스크랩 · 좋아요 · 댓글 기록 보기</span>
          <span className="mypage-activity-menu-arrow">›</span>
        </button>
      </section>
    </div>
  );
}
