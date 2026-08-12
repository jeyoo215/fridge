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
import "./CommunityPostDetail.css";

const TEMP_USER_ID = 1; // TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체

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
      fetchCommunityPost(postId),
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
    const res = await toggleCommunityPostLike(TEMP_USER_ID, postId);
    setLiked(res.active);
    setLikeCount(res.count);
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
      navigate("/community");
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

  return (
    <article className="community-detail-container">
      <div className="community-detail-top">
        <div className="community-detail-author">사용자 {post.userId}</div>
        <div className="community-detail-actions">
          <button type="button" onClick={() => navigate(`/community/${postId}/edit`)}>
            수정
          </button>
          <button type="button" className="danger" onClick={handleDelete}>
            삭제
          </button>
        </div>
      </div>
      <h2 className="community-detail-title">{post.title}</h2>
      <div className="community-detail-date">{post.createdAt?.slice(0, 10)}</div>

      {post.sections.map((section, index) => (
        <section className="community-detail-section" key={index}>
          <h3 className="community-detail-subtitle">{section.subtitle}</h3>
          {section.mediaUrl && section.mediaType === "VIDEO" && (
            <video className="community-detail-image" src={toMediaSrc(section.mediaUrl)} controls />
          )}
          {section.mediaUrl && section.mediaType === "IMAGE" && (
            <img className="community-detail-image" src={toMediaSrc(section.mediaUrl)} alt={section.subtitle} />
          )}
          <div
            className="community-detail-content"
            dangerouslySetInnerHTML={{ __html: section.content }}
          />
        </section>
      ))}

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
