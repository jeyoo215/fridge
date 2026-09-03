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
import { getUserId, isLoggedIn } from "../api/authApi";
import { REPORT_REASONS, reportPost, reportComment } from "../api/communityReportApi";
import "./CommunityPostDetail.css";

// 댓글 목록에서 "내가 쓴 댓글인지" 비교할 때 씀. 토큰 안 userId는 문자열이라 Number로 맞춰줌.
const currentUserId = Number(getUserId());

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
  const [replyingTo, setReplyingTo] = useState(null); // 답글 입력창을 열어둔 댓글의 commentId
  const [replyText, setReplyText] = useState("");
  const [reportingPost, setReportingPost] = useState(false); // 게시글 신고 입력창 열림 여부
  const [postReportReason, setPostReportReason] = useState(REPORT_REASONS[0]);
  const [reportingCommentId, setReportingCommentId] = useState(null); // 신고 입력창을 열어둔 댓글의 commentId
  const [commentReportReason, setCommentReportReason] = useState(REPORT_REASONS[0]);
  const [reportNotice, setReportNotice] = useState(null); // 신고 접수 완료 안내 문구
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      fetchCommunityPost(postId),
      fetchCommunityPostLikeStatus(postId),
      fetchCommunityPostScrapStatus(postId),
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
    if (!isLoggedIn()) {
      navigate("/login");
      return;
    }
    try {
      const res = await toggleCommunityPostLike(postId);
      setLiked(res.active);
      setLikeCount(res.count);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleToggleScrap = async () => {
    if (!isLoggedIn()) {
      navigate("/login");
      return;
    }
    const res = await toggleCommunityPostScrap(postId);
    setScrapped(res.active);
    setScrapCount(res.count);
  };

  const handleDelete = async () => {
    if (!window.confirm("이 글을 삭제할까요? 되돌릴 수 없습니다.")) return;
    try {
      await deleteCommunityPost(postId);
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
      await createCommunityPostComment(postId, content);
      setNewComment("");
      setComments(await fetchCommunityPostComments(postId));
    } catch (err) {
      setError(err.message);
    }
  };

  // 대댓글 등록. 새로 단 답글의 닉네임을 직접 알 필요 없이, 등록 후 목록을 다시 받아와서
  // 서버가 채워준 nickname/parentCommentId 그대로 반영한다.
  const handleAddReply = async (parentCommentId) => {
    const content = replyText.trim();
    if (!content) return;
    try {
      await createCommunityPostComment(postId, content, parentCommentId);
      setReplyText("");
      setReplyingTo(null);
      setComments(await fetchCommunityPostComments(postId));
    } catch (err) {
      setError(err.message);
    }
  };

  const handleReportPost = async (e) => {
    e.preventDefault();
    try {
      await reportPost(postId, postReportReason);
      setReportingPost(false);
      setReportNotice("신고가 접수됐습니다. 검토 후 처리됩니다.");
    } catch (err) {
      setError(err.message);
    }
  };

  const handleReportComment = async (e, commentId) => {
    e.preventDefault();
    try {
      await reportComment(commentId, commentReportReason);
      setReportingCommentId(null);
      setReportNotice("신고가 접수됐습니다. 검토 후 처리됩니다.");
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm("이 댓글을 삭제하시겠습니까? 되돌릴 수 없습니다.")) return;
    try {
      await deleteCommunityPostComment(commentId);
      // 원댓글을 지우면 백엔드가 대댓글도 같이 지우므로, 화면에서도 같이 걷어냄
      setComments((prev) =>
        prev.filter((comment) => comment.commentId !== commentId && comment.parentCommentId !== commentId)
      );
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
        <div className="community-detail-author">{post.nickname}</div>
        {!post.promotedRecipeId && post.userId === currentUserId && (
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
        {post.userId !== currentUserId && (
          <button
            type="button"
            className="community-detail-report-button"
            onClick={() => {
              if (!isLoggedIn()) {
                navigate("/login");
                return;
              }
              setReportingPost((prev) => !prev);
            }}
          >
            🚨 신고
          </button>
        )}
      </div>

      {reportNotice && <p className="community-detail-report-notice">{reportNotice}</p>}

      {reportingPost && (
        <form className="community-detail-report-form community-detail-report-form--centered" onSubmit={handleReportPost}>
          <select value={postReportReason} onChange={(e) => setPostReportReason(e.target.value)}>
            {REPORT_REASONS.map((reason) => (
              <option key={reason} value={reason}>
                {reason}
              </option>
            ))}
          </select>
          <button type="submit">신고하기</button>
        </form>
      )}

      <section id="comments" className="community-detail-comments">
        <h3 className="community-detail-comments-title">댓글 {comments.length}</h3>
        {comments.length === 0 ? (
          <p className="community-detail-comments-empty">아직 댓글이 없어요. 첫 댓글을 남겨보세요!</p>
        ) : (
          <ul className="community-detail-comment-list">
            {comments
              .filter((comment) => !comment.parentCommentId)
              .map((comment) => {
                const replies = comments.filter((reply) => reply.parentCommentId === comment.commentId);
                return (
                  <li key={comment.commentId} className="community-detail-comment">
                    <div className="community-detail-comment-row">
                      <div className="community-detail-comment-body">
                        <div className="community-detail-comment-meta">
                          <span className="community-detail-comment-author">{comment.nickname}</span>
                          <span className="community-detail-comment-date">{comment.createdAt?.slice(0, 10)}</span>
                        </div>
                        <p className="community-detail-comment-content">{comment.content}</p>
                      </div>
                      {comment.userId === currentUserId && (
                        <button
                          type="button"
                          className="community-detail-comment-delete"
                          onClick={() => handleDeleteComment(comment.commentId)}
                          aria-label="댓글 삭제"
                        >
                          ×
                        </button>
                      )}
                    </div>
                    <button
                      type="button"
                      className="community-detail-comment-reply-toggle"
                      onClick={() => {
                        if (!isLoggedIn()) {
                          navigate("/login");
                          return;
                        }
                        setReplyingTo(replyingTo === comment.commentId ? null : comment.commentId);
                        setReplyText("");
                      }}
                    >
                      답글
                    </button>
                    {comment.userId !== currentUserId && (
                      <button
                        type="button"
                        className="community-detail-comment-report-toggle"
                        onClick={() => {
                          if (!isLoggedIn()) {
                            navigate("/login");
                            return;
                          }
                          setReportingCommentId(reportingCommentId === comment.commentId ? null : comment.commentId);
                        }}
                      >
                        신고
                      </button>
                    )}

                    {replyingTo === comment.commentId && (
                      <form
                        className="community-detail-reply-form"
                        onSubmit={(e) => {
                          e.preventDefault();
                          handleAddReply(comment.commentId);
                        }}
                      >
                        <input
                          type="text"
                          placeholder={`${comment.nickname}님에게 답글 남기기`}
                          value={replyText}
                          onChange={(e) => setReplyText(e.target.value)}
                          maxLength={500}
                          autoFocus
                        />
                        <button type="submit">등록</button>
                      </form>
                    )}

                    {reportingCommentId === comment.commentId && (
                      <form
                        className="community-detail-report-form"
                        onSubmit={(e) => handleReportComment(e, comment.commentId)}
                      >
                        <select value={commentReportReason} onChange={(e) => setCommentReportReason(e.target.value)}>
                          {REPORT_REASONS.map((reason) => (
                            <option key={reason} value={reason}>
                              {reason}
                            </option>
                          ))}
                        </select>
                        <button type="submit">신고하기</button>
                      </form>
                    )}

                    {replies.length > 0 && (
                      <ul className="community-detail-reply-list">
                        {replies.map((reply) => (
                          <li key={reply.commentId} className="community-detail-comment community-detail-reply">
                            <div className="community-detail-comment-row">
                              <div className="community-detail-comment-body">
                                <div className="community-detail-comment-meta">
                                  <span className="community-detail-comment-author">{reply.nickname}</span>
                                  <span className="community-detail-comment-date">{reply.createdAt?.slice(0, 10)}</span>
                                </div>
                                <p className="community-detail-comment-content">{reply.content}</p>
                              </div>
                              {reply.userId === currentUserId && (
                                <button
                                  type="button"
                                  className="community-detail-comment-delete"
                                  onClick={() => handleDeleteComment(reply.commentId)}
                                  aria-label="답글 삭제"
                                >
                                  ×
                                </button>
                              )}
                            </div>
                            {reply.userId !== currentUserId && (
                              <button
                                type="button"
                                className="community-detail-comment-report-toggle"
                                onClick={() => {
                                  if (!isLoggedIn()) {
                                    navigate("/login");
                                    return;
                                  }
                                  setReportingCommentId(reportingCommentId === reply.commentId ? null : reply.commentId);
                                }}
                              >
                                신고
                              </button>
                            )}
                            {reportingCommentId === reply.commentId && (
                              <form
                                className="community-detail-report-form"
                                onSubmit={(e) => handleReportComment(e, reply.commentId)}
                              >
                                <select value={commentReportReason} onChange={(e) => setCommentReportReason(e.target.value)}>
                                  {REPORT_REASONS.map((reason) => (
                                    <option key={reason} value={reason}>
                                      {reason}
                                    </option>
                                  ))}
                                </select>
                                <button type="submit">신고하기</button>
                              </form>
                            )}
                          </li>
                        ))}
                      </ul>
                    )}
                  </li>
                );
              })}
          </ul>
        )}
        {isLoggedIn() ? (
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
        ) : (
          <p className="community-detail-comments-empty">
            댓글을 작성하려면{" "}
            <button type="button" className="community-detail-comment-login-link" onClick={() => navigate("/login")}>
              로그인
            </button>
            이 필요합니다.
          </p>
        )}
      </section>
    </article>
  );
}
