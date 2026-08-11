import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchCommunityPosts, toMediaSrc } from "../api/communityApi";
import "./CommunityList.css";

export default function CommunityList() {
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    fetchCommunityPosts(page)
      .then((data) => {
        setPosts(data.content);
        setTotalPages(data.totalPages);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [page]);

  return (
    <div className="community-list-container">
      <div className="community-list-header">
        <h2 className="community-list-title">커뮤니티</h2>
        <button className="community-write-button" onClick={() => navigate("/community/new")}>
          ✏️ 글쓰기
        </button>
      </div>

      {loading && <p className="community-list-status">불러오는 중...</p>}
      {error && <p className="community-list-status error">{error}</p>}
      {!loading && !error && posts.length === 0 && (
        <p className="community-list-status">아직 등록된 글이 없어요. 첫 글을 남겨보세요!</p>
      )}

      <div className="community-post-cards">
        {posts.map((post) => (
          <div
            key={post.postId}
            className="community-post-card"
            onClick={() => navigate(`/community/${post.postId}`)}
          >
            <div className="community-post-card-body">
              <div className="community-post-card-author">사용자 {post.userId}</div>
              <div className="community-post-card-title">{post.title}</div>
              <p className="community-post-card-preview">{post.previewText}</p>
              <div className="community-post-card-meta">
                <span>{post.createdAt?.slice(0, 10)}</span>
                <span>공감 {post.likeCount}</span>
              </div>
            </div>
            {post.thumbnailUrl && (
              <div className="community-post-card-thumbnail">
                <img src={toMediaSrc(post.thumbnailUrl)} alt="" />
              </div>
            )}
          </div>
        ))}
      </div>

      {totalPages > 1 && (
        <div className="community-pagination">
          <button
            type="button"
            className="community-pagination-arrow"
            disabled={page === 0}
            onClick={() => setPage((prev) => prev - 1)}
          >
            이전
          </button>
          {Array.from({ length: totalPages }, (_, i) => i).map((pageNumber) => (
            <button
              type="button"
              key={pageNumber}
              className={`community-pagination-page${pageNumber === page ? " active" : ""}`}
              onClick={() => setPage(pageNumber)}
            >
              {pageNumber + 1}
            </button>
          ))}
          <button
            type="button"
            className="community-pagination-arrow"
            disabled={page >= totalPages - 1}
            onClick={() => setPage((prev) => prev + 1)}
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}
