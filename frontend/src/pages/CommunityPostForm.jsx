import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import ReactQuill, { Quill } from "react-quill-new";
import "react-quill-new/dist/quill.snow.css";
import {
  createCommunityPost,
  updateCommunityPost,
  fetchCommunityPost,
  uploadCommunityMedia,
  toMediaSrc,
} from "../api/communityApi";
import { searchIngredients } from "../api/ingredientApi";
import { fetchRecipeCategories } from "../api/recipeApi";
import "./CommunityPostForm.css";

const TEMP_USER_ID = 1; // TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체

const MAX_IMAGE_SIZE = 50 * 1024 * 1024;  // 50MB
const MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB

const DIFFICULTY_OPTIONS = ["쉬움", "보통", "어려움"];

// Quill 자체 드롭다운 툴바(크기/색상)가 이 환경에서 클릭하자마자 닫혀버리는 문제가 있어서,
// 크기/색상은 Quill 툴바 대신 별도의 숫자 입력창 + 적용 버튼으로 직접 만든다.
const SizeStyle = Quill.import("attributors/style/size");
// 기본 SizeStyle엔 ['10px','18px','32px']라는 whitelist가 미리 박혀있어서,
// 지우지 않으면 그 3개 값 외엔(예: "30pt") 조용히 무시되고 적용되지 않는다.
SizeStyle.whitelist = null;
Quill.register(SizeStyle, true);
const ColorStyle = Quill.import("attributors/style/color");
Quill.register(ColorStyle, true);

const QUILL_MODULES = {
  toolbar: [
    ["bold", "italic"],
    // 정렬도 드롭다운(picker)이 아니라 개별 버튼으로 구성 (드롭다운 방식은 이 환경에서 불안정함)
    [{ align: "" }, { align: "center" }, { align: "right" }, { align: "justify" }],
  ],
};

function emptySection() {
  return { key: crypto.randomUUID(), subtitle: "", content: "", mediaUrl: "", mediaType: null, uploading: false };
}

function emptyStep() {
  return { key: crypto.randomUUID(), description: "" };
}

export default function CommunityPostForm() {
  const { postId } = useParams(); // 있으면 수정 모드, 없으면 새 글 작성 모드
  const isEditMode = Boolean(postId);

  // 섹션별 Quill 인스턴스 / 크기·색상 입력창을 key로 찾아가기 위한 저장소 (렌더링과 무관해서 ref로 관리)
  const editorRefs = useRef({});
  const getBucket = (key) => {
    if (!editorRefs.current[key]) {
      editorRefs.current[key] = {};
    }
    return editorRefs.current[key];
  };

  const [title, setTitle] = useState("");
  const [sections, setSections] = useState([emptySection()]);
  const [loading, setLoading] = useState(isEditMode);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [locked, setLocked] = useState(false); // 정식 레시피로 승격된 글 (수정 불가)
  const navigate = useNavigate();

  // 레시피 기본 정보
  const [categories, setCategories] = useState([]);
  const [categoryId, setCategoryId] = useState("");
  const [cookingTimeMinutes, setCookingTimeMinutes] = useState("");
  const [difficulty, setDifficulty] = useState(DIFFICULTY_OPTIONS[0]);

  // 재료 목록 + 재료 검색
  const [ingredients, setIngredients] = useState([]); // { key, ingredientId, ingredientName, quantity, unit }
  const [ingredientKeyword, setIngredientKeyword] = useState("");
  const [ingredientResults, setIngredientResults] = useState([]);

  // 조리순서 목록
  const [steps, setSteps] = useState([emptyStep()]);

  // 카테고리 목록이 나중에 도착해도 이름 매칭이 되도록 별도 상태로 들고 있다가 반영한다.
  const [pendingCategoryName, setPendingCategoryName] = useState(null);

  useEffect(() => {
    fetchRecipeCategories().then(setCategories).catch(() => {});
  }, []);

  useEffect(() => {
    if (!isEditMode) return;
    fetchCommunityPost(postId)
      .then((post) => {
        if (post.promotedRecipeId) {
          setLocked(true);
          return;
        }
        setTitle(post.title);
        setCookingTimeMinutes(String(post.cookingTimeMinutes ?? ""));
        setDifficulty(post.difficulty || DIFFICULTY_OPTIONS[0]);
        setIngredients(
          post.ingredients.map((item) => ({
            key: crypto.randomUUID(),
            ingredientId: item.ingredientId,
            ingredientName: item.ingredientName,
            quantity: item.quantity ?? "",
            unit: item.unit || "",
          }))
        );
        setSteps(
          post.steps.length > 0
            ? post.steps.map((step) => ({ key: crypto.randomUUID(), description: step.description }))
            : [emptyStep()]
        );
        setSections(
          post.sections.map((s) => ({
            key: crypto.randomUUID(),
            subtitle: s.subtitle,
            content: s.content,
            mediaUrl: s.mediaUrl || "",
            mediaType: s.mediaType,
            uploading: false,
          }))
        );
        // categoryId는 상세 응답에 이름만 내려와서, 카테고리 목록에서 이름으로 찾아 채운다.
        setPendingCategoryName(post.categoryName);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [isEditMode, postId]);

  useEffect(() => {
    if (!pendingCategoryName || categories.length === 0) return;
    const matched = categories.find((c) => c.categoryName === pendingCategoryName);
    if (matched) setCategoryId(String(matched.categoryId));
    setPendingCategoryName(null);
  }, [pendingCategoryName, categories]);

  // 재료 검색어 입력하고 300ms 있다가 검색 (IngredientRegisterForm과 동일한 디바운스 패턴)
  useEffect(() => {
    if (!ingredientKeyword) {
      setIngredientResults([]);
      return;
    }
    const timer = setTimeout(() => {
      searchIngredients(ingredientKeyword)
        .then(setIngredientResults)
        .catch(() => setIngredientResults([]));
    }, 300);
    return () => clearTimeout(timer);
  }, [ingredientKeyword]);

  const addIngredientRow = (ingredient) => {
    setIngredients((prev) => [
      ...prev,
      { key: crypto.randomUUID(), ingredientId: ingredient.ingredientId, ingredientName: ingredient.ingredientName, quantity: "", unit: "" },
    ]);
    setIngredientKeyword("");
    setIngredientResults([]);
  };

  const updateIngredientRow = (key, patch) => {
    setIngredients((prev) => prev.map((row) => (row.key === key ? { ...row, ...patch } : row)));
  };

  const removeIngredientRow = (key) => {
    setIngredients((prev) => prev.filter((row) => row.key !== key));
  };

  const addStep = () => setSteps((prev) => [...prev, emptyStep()]);

  const updateStep = (key, description) => {
    setSteps((prev) => prev.map((step) => (step.key === key ? { ...step, description } : step)));
  };

  const removeStep = (key) => {
    setSteps((prev) => (prev.length > 1 ? prev.filter((step) => step.key !== key) : prev));
  };

  const updateSection = (key, patch) => {
    setSections((prev) => prev.map((s) => (s.key === key ? { ...s, ...patch } : s)));
  };

  const addSection = () => setSections((prev) => [...prev, emptySection()]);

  const removeSection = (key) => {
    setSections((prev) => (prev.length > 1 ? prev.filter((s) => s.key !== key) : prev));
  };

  // 적용 버튼은 에디터 바깥에 있어서 클릭하는 순간 에디터가 포커스를 잃는데,
  // getSelection(true)를 쓰면 Quill이 마지막으로 드래그해뒀던 선택 범위를 다시 불러와서 그대로 적용해준다.
  // getEditor()는 ReactQuill이 마운트된 뒤에만 유효해서, ref 콜백이 아니라 실제로 쓰는 이 시점에 호출한다.
  const applyFontSize = (key) => {
    const { quillComponent, sizeInput } = getBucket(key);
    const quill = quillComponent?.getEditor();
    if (!quill || !sizeInput) return;
    const pt = Math.min(50, Math.max(5, Number(sizeInput.value) || 16));
    const range = quill.getSelection(true);
    if (!range || range.length === 0) {
      setError("크기를 바꿀 글자를 먼저 드래그해서 선택해주세요.");
      return;
    }
    setError(null);
    quill.formatText(range.index, range.length, "size", `${pt}pt`, "user");
  };

  const applyColor = (key) => {
    const { quillComponent, colorInput } = getBucket(key);
    const quill = quillComponent?.getEditor();
    if (!quill || !colorInput) return;
    const range = quill.getSelection(true);
    if (!range || range.length === 0) {
      setError("색상을 바꿀 글자를 먼저 드래그해서 선택해주세요.");
      return;
    }
    setError(null);
    quill.formatText(range.index, range.length, "color", colorInput.value, "user");
  };

  const handleFileSelected = async (key, file) => {
    if (!file) return;

    const isVideo = file.type.startsWith("video/");
    const isImage = file.type.startsWith("image/");
    if (!isImage && !isVideo) {
      setError("이미지 또는 동영상 파일만 첨부할 수 있습니다.");
      return;
    }
    const limit = isVideo ? MAX_VIDEO_SIZE : MAX_IMAGE_SIZE;
    if (file.size > limit) {
      setError(`${isVideo ? "동영상" : "이미지"}은 ${isVideo ? "100MB" : "50MB"}를 넘을 수 없습니다.`);
      return;
    }

    setError(null);
    updateSection(key, { uploading: true });
    try {
      const { url, mediaType } = await uploadCommunityMedia(file);
      updateSection(key, { mediaUrl: url, mediaType, uploading: false });
    } catch (err) {
      setError(err.message);
      updateSection(key, { uploading: false });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) {
      setError("제목을 입력해주세요.");
      return;
    }
    if (!categoryId) {
      setError("카테고리를 선택해주세요.");
      return;
    }
    if (!cookingTimeMinutes || Number(cookingTimeMinutes) <= 0) {
      setError("조리시간을 입력해주세요.");
      return;
    }
    if (ingredients.length === 0) {
      setError("재료를 1개 이상 추가해주세요.");
      return;
    }
    if (steps.some((s) => !s.description.trim())) {
      setError("모든 조리순서를 입력해주세요.");
      return;
    }
    if (sections.some((s) => !s.subtitle.trim() || !s.content.trim() || s.content === "<p><br></p>")) {
      setError("모든 섹션의 소제목과 내용을 입력해주세요.");
      return;
    }
    if (sections.some((s) => s.uploading)) {
      setError("파일 업로드가 끝날 때까지 기다려주세요.");
      return;
    }

    setSubmitting(true);
    setError(null);
    const payload = {
      title,
      categoryId: Number(categoryId),
      cookingTimeMinutes: Number(cookingTimeMinutes),
      difficulty,
      sections: sections.map(({ subtitle, content, mediaUrl, mediaType }) => ({
        subtitle,
        content,
        mediaUrl: mediaUrl || null,
        mediaType,
      })),
      ingredients: ingredients.map(({ ingredientId, quantity, unit }) => ({
        ingredientId,
        quantity: quantity === "" ? null : Number(quantity),
        unit: unit || null,
      })),
      steps: steps.map(({ description }) => ({ description })),
    };
    try {
      if (isEditMode) {
        await updateCommunityPost(TEMP_USER_ID, postId, payload);
        navigate(`/community/${postId}`);
      } else {
        const { postId: newPostId } = await createCommunityPost(TEMP_USER_ID, payload);
        navigate(`/community/${newPostId}`);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <p className="community-form-status">불러오는 중...</p>;

  if (locked) {
    return (
      <div className="community-form-status">
        <p>정식 레시피로 등록된 게시글은 수정할 수 없습니다.</p>
        <button type="button" onClick={() => navigate(`/community/${postId}`)}>
          게시글로 돌아가기
        </button>
      </div>
    );
  }

  return (
    <form className="community-form" onSubmit={handleSubmit}>
      <h2 className="community-form-title">{isEditMode ? "레시피 글 수정" : "레시피 글쓰기"}</h2>

      <label className="community-form-label">
        게시글 제목
        <input
          className="community-form-input"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="예: 감바스 알 하이요 제대로 만드는 법"
        />
      </label>

      <div className="community-form-recipe-meta">
        <label className="community-form-label">
          카테고리
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
            <option value="">선택</option>
            {categories.map((category) => (
              <option key={category.categoryId} value={category.categoryId}>
                {category.categoryName}
              </option>
            ))}
          </select>
        </label>
        <label className="community-form-label">
          조리시간(분)
          <input
            type="number"
            min="1"
            value={cookingTimeMinutes}
            onChange={(e) => setCookingTimeMinutes(e.target.value)}
          />
        </label>
        <label className="community-form-label">
          난이도
          <select value={difficulty} onChange={(e) => setDifficulty(e.target.value)}>
            {DIFFICULTY_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="community-form-recipe-block">
        <h3 className="community-form-block-title">재료</h3>
        {ingredients.length > 0 && (
          <ul className="community-form-ingredient-list">
            {ingredients.map((row) => (
              <li key={row.key} className="community-form-ingredient-row">
                <span className="community-form-ingredient-name">{row.ingredientName}</span>
                <input
                  type="number"
                  placeholder="수량"
                  value={row.quantity}
                  onChange={(e) => updateIngredientRow(row.key, { quantity: e.target.value })}
                />
                <input
                  type="text"
                  placeholder="단위 (예: 개)"
                  value={row.unit}
                  onChange={(e) => updateIngredientRow(row.key, { unit: e.target.value })}
                />
                <button type="button" onClick={() => removeIngredientRow(row.key)}>
                  삭제
                </button>
              </li>
            ))}
          </ul>
        )}
        <div className="community-form-ingredient-search">
          <input
            type="text"
            placeholder="재료 이름으로 검색해서 추가"
            value={ingredientKeyword}
            onChange={(e) => setIngredientKeyword(e.target.value)}
          />
          {ingredientResults.length > 0 && (
            <ul className="community-form-ingredient-search-results">
              {ingredientResults.map((ingredient) => (
                <li key={ingredient.ingredientId}>
                  <button type="button" onClick={() => addIngredientRow(ingredient)}>
                    {ingredient.ingredientName}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <div className="community-form-recipe-block">
        <h3 className="community-form-block-title">조리순서</h3>
        {steps.map((step, index) => (
          <div className="community-form-step-row" key={step.key}>
            <span className="community-form-step-number">{index + 1}</span>
            <textarea
              value={step.description}
              onChange={(e) => updateStep(step.key, e.target.value)}
              placeholder={`${index + 1}단계 설명`}
            />
            {steps.length > 1 && (
              <button type="button" onClick={() => removeStep(step.key)}>
                삭제
              </button>
            )}
          </div>
        ))}
        <button type="button" className="community-form-add-section" onClick={addStep}>
          + 조리순서 추가
        </button>
      </div>

      {sections.map((section, index) => (
        <div className="community-form-section" key={section.key}>
          <div className="community-form-section-header">
            <span>섹션 {index + 1}</span>
            {sections.length > 1 && (
              <button type="button" className="community-form-remove" onClick={() => removeSection(section.key)}>
                삭제
              </button>
            )}
          </div>

          <label className="community-form-label">
            소제목
            <input
              className="community-form-input"
              value={section.subtitle}
              onChange={(e) => updateSection(section.key, { subtitle: e.target.value })}
              placeholder="예: 재료 준비"
            />
          </label>

          <label className="community-form-label">
            사진/동영상 첨부 (선택, 이미지 50MB / 동영상 100MB까지)
            <input
              type="file"
              accept="image/*,video/*"
              onChange={(e) => handleFileSelected(section.key, e.target.files?.[0])}
            />
          </label>

          {section.uploading && <p className="community-form-upload-status">업로드 중...</p>}

          {section.mediaUrl && !section.uploading && (
            <div className="community-form-preview">
              {section.mediaType === "VIDEO" ? (
                <video src={toMediaSrc(section.mediaUrl)} controls />
              ) : (
                <img src={toMediaSrc(section.mediaUrl)} alt="첨부 미리보기" />
              )}
            </div>
          )}

          <div className="community-form-text-tools">
            <label>
              글자 크기(pt)
              <input
                type="number"
                min="5"
                max="50"
                defaultValue="16"
                ref={(el) => (getBucket(section.key).sizeInput = el)}
              />
            </label>
            <button type="button" onClick={() => applyFontSize(section.key)}>
              크기 적용
            </button>
            <label>
              글자 색
              <input
                type="color"
                defaultValue="#000000"
                ref={(el) => (getBucket(section.key).colorInput = el)}
              />
            </label>
            <button type="button" onClick={() => applyColor(section.key)}>
              색상 적용
            </button>
          </div>
          <p className="community-form-hint">
            먼저 아래 내용에서 바꿀 글자를 드래그해서 선택한 다음, 위 버튼을 눌러주세요.
          </p>

          <label className="community-form-label">
            내용
            <ReactQuill
              ref={(el) => {
                getBucket(section.key).quillComponent = el;
              }}
              theme="snow"
              value={section.content}
              onChange={(value) => updateSection(section.key, { content: value })}
              modules={QUILL_MODULES}
            />
          </label>
        </div>
      ))}

      <button type="button" className="community-form-add-section" onClick={addSection}>
        + 섹션 추가
      </button>

      {error && <p className="community-form-error">{error}</p>}

      <button type="submit" className="community-form-submit" disabled={submitting}>
        {submitting ? "저장 중..." : isEditMode ? "수정 완료" : "게시글 등록"}
      </button>
    </form>
  );
}
