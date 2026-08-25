import { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import {
  fetchCommunityPost,
  fetchCommunityPostLikeStatus,
  toggleCommunityPostLike,
  fetchCommunityPostScrapStatus,
  toggleCommunityPostScrap,
  deleteCommunityPost,
  fetchCommunityPostComments,
  createCommunityPostComment,
  deleteCommunityPostComment,
  toMediaSrc,
} from "../api/communityApi";
import { getBoardConfig } from "./communityBoards";
import { getCurrentUserId } from "../api/authApi";
import "./CommunityPostDetail.css";

const TEMP_USER_ID = getCurrentUserId() ?? 1; // 로그인 안 했으면 1(seed 계정)로 폴백

export default function CommunityPostDetail() {
  const { postId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [scrapped, setScrapped] = useState(false);
  const [scrapCount, setScrapCount] = useState(0);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      fetchCommunityPost(postId, TEMP_USER_ID),
      fetchCommunityPostLikeStatus(TEMP_USER_ID, postId),
      fetchCommunityPostScrapStatus(TEMP_USER_ID, postId),
      fetchCommunityPostComments(postId),
    ])
      .then(([postRes, likeRes, scrapRes, commentsRes]) => {
        setPost(postRes);
        setLiked(likeRes.active);
        setLikeCount(likeRes.count);
        setScrapped(scrapRes.active);
        setScrapCount(scrapRes.count);
        setComments(commentsRes);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [postId]);

  useEffect(() => {
    if (loading || location.hash !== "#comments") return;
    document.getElementById("comments")?.scrollIntoView({ behavior: "smooth" });
  }, [loading, location.hash]);

  const handleToggleLike = async () => {
    try {
      const res = await toggleCommunityPostLike(TEMP_USER_ID, postId);
      setLiked(res.active);
      setLikeCount(res.count);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleToggleScrap = async () => {
    const res = await toggleCommunityPostScrap(TEMP_USER_ID, postId);
    setScrapped(res.active);
    setScrapCount(res.count);
  };

  const handleDelete = async () => {
    if (!window.confirm("이 글을 삭제할까요? 되돌릴 수 없습니다.")) return;
    try {
      await deleteCommunityPost(TEMP_USER_ID, postId);
      navigate(getBoardConfig(post.boardType).listPath);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    const content = newComment.trim();
    if (!content) return;
    try {
      const { commentId } = await createCommunityPostComment(TEMP_USER_ID, postId, content);
      setComments((prev) => [
        ...prev,
        { commentId, userId: TEMP_USER_ID, content, createdAt: new Date().toISOString() },
      ]);
      setNewComment("");
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteComment = async (commentId) => {
    try {
      await deleteCommunityPostComment(TEMP_USER_ID, commentId);
      setComments((prev) => prev.filter((comment) => comment.commentId !== commentId));
    } catch (err) {
      setError(err.message);
    }
  };

  if (loading) return <p className="community-detail-status">불러오는 중...</p>;
  if (error) return <p className="community-detail-status error">{error}</p>;
  if (!post) return null;

  const board = getBoardConfig(post.boardType);
  const isRecipeBoard = post.boardType === "RECIPE";

  return (
    <article className="community-detail-container">
      <button type="button" className="community-detail-back" onClick={() => navigate(board.listPath)}>
        ← 목록으로
      </button>

      <div className="community-detail-top">
        <div className="community-detail-author">사용자 {post.userId}</div>
        {!post.promotedRecipeId && (
          <div className="community-detail-actions">
            <button type="button" onClick={() => navigate(`/community/${postId}/edit`)}>
              수정
            </button>
            <button type="button" className="danger" onClick={handleDelete}>
              삭제
            </button>
          </div>
        )}
      </div>
      <h2 className="community-detail-title">
        {post.prefix && <span className="community-detail-prefix">{post.prefix}</span>}
        {post.title}
      </h2>
      <div className="community-detail-date">{post.createdAt?.slice(0, 10)}</div>

      {post.promotedRecipeId && (
        <button
          type="button"
          className="community-detail-promoted-banner"
          onClick={() => navigate(`/recipes/${post.promotedRecipeId}`)}
        >
          🏅 추천을 많이 받아 정식 레시피로 등록됐어요 · 레시피 보러가기 →
        </button>
      )}

      {isRecipeBoard && (
        <>
          <div className="community-detail-recipe-meta">
            <span>🏷 {post.categoryName}</span>
            <span>⏱ {post.cookingTimeMinutes}분</span>
            <span>· {post.difficulty}</span>
          </div>

          <section className="community-detail-recipe-block">
            <h3 className="community-detail-block-title">재료</h3>
            <ul className="community-detail-ingredient-list">
              {post.ingredients.map((item) => (
                <li key={item.ingredientId}>
                  <span>{item.ingredientName}</span>
                  <span className="community-detail-ingredient-amount">
                    {item.quantity ?? ""} {item.unit || ""}
                  </span>
                </li>
              ))}
            </ul>
          </section>
        </>
      )}

      <section className="community-detail-recipe-block">
        {isRecipeBoard && <h3 className="community-detail-block-title">조리순서</h3>}
        {post.steps.map((step, index) => (
          <div className="community-detail-section" key={step.stepOrder}>
            {isRecipeBoard && <span className="community-detail-step-badge">{index + 1}단계</span>}
            {step.mediaUrl && step.mediaType === "VIDEO" && (
              <video className="community-detail-image" src={toMediaSrc(step.mediaUrl)} controls />
            )}
            {step.mediaUrl && step.mediaType === "IMAGE" && (
              <img className="community-detail-image" src={toMediaSrc(step.mediaUrl)} alt={`${index + 1}단계`} />
            )}
            <div
              className="community-detail-content"
              dangerouslySetInnerHTML={{ __html: step.description }}
            />
          </div>
        ))}
      </section>

      <div className="community-detail-social">
        <button
          type="button"
          className={`community-detail-like-button ${liked ? "active" : ""}`}
          onClick={handleToggleLike}
        >
          {liked ? "❤️" : "🤍"} 공감 {likeCount}
        </button>
        <button
          type="button"
          className={`community-detail-scrap-button ${scrapped ? "active" : ""}`}
          onClick={handleToggleScrap}
        >
          {scrapped ? "🔖" : "📑"} 스크랩 {scrapCount}
        </button>
      </div>

      <section id="comments" className="community-detail-comments">
        <h3 className="community-detail-comments-title">댓글 {comments.length}</h3>
        {comments.length === 0 ? (
          <p className="community-detail-comments-empty">아직 댓글이 없어요. 첫 댓글을 남겨보세요!</p>
        ) : (
          <ul className="community-detail-comment-list">
            {comments.map((comment) => (
              <li key={comment.commentId} className="community-detail-comment">
                <div className="community-detail-comment-body">
                  <div className="community-detail-comment-meta">
                    <span className="community-detail-comment-author">사용자 {comment.userId}</span>
                    <span className="community-detail-comment-date">{comment.createdAt?.slice(0, 10)}</span>
                  </div>
                  <p className="community-detail-comment-content">{comment.content}</p>
                </div>
                {comment.userId === TEMP_USER_ID && (
                  <button
                    type="button"
                    className="community-detail-comment-delete"
                    onClick={() => handleDeleteComment(comment.commentId)}
                    aria-label="댓글 삭제"
                  >
                    ×
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
        <form className="community-detail-comment-form" onSubmit={handleAddComment}>
          <input
            type="text"
            placeholder="댓글을 입력하세요"
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            maxLength={500}
          />
          <button type="submit">등록</button>
        </form>
      </section>
    </article>
  );
}
