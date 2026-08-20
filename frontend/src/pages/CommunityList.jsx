import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchCommunityPosts, toMediaSrc } from "../api/communityApi";
import CommunitySidebar from "../component/CommunitySidebar";
import { getBoardConfig, FREE_TALK_PREFIXES } from "./communityBoards";
import "./CommunityList.css";

const TEMP_USER_ID = 1; // TODO: 로그인 기능 만들어지면 실제 로그인한 유저 ID로 교체

export default function CommunityList({ boardType = "RECIPE" }) {
  const board = getBoardConfig(boardType);
  const isChallengeBoard = board.challengeType != null;

  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [sortBy, setSortBy] = useState("latest"); // "latest" | "popular"
  const [prefix, setPrefix] = useState(""); // FREE_TALK 게시판 말머리 필터 ("" = 전체)
  const [keyword, setKeyword] = useState(""); // 실제 검색에 쓰이는 확정된 검색어 (입력 중엔 반영 안 됨)
  const [keywordInput, setKeywordInput] = useState(""); // 검색창 입력값
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [accessDenied, setAccessDenied] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    setError(null);
    setAccessDenied(false);
    fetchCommunityPosts(page, 10, sortBy, boardType, {
      prefix: prefix || undefined,
      keyword: keyword || undefined,
      userId: TEMP_USER_ID,
    })
      .then((data) => {
        setPosts(data.content);
        setTotalPages(data.totalPages);
      })
      .catch((err) => {
        if (isChallengeBoard) {
          setAccessDenied(true);
        } else {
          setError(err.message);
        }
      })
      .finally(() => setLoading(false));
  }, [page, sortBy, boardType, prefix, keyword, isChallengeBoard]);

  const changeSortBy = (value) => {
    setSortBy(value);
    setPage(0);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setKeyword(keywordInput.trim());
    setPage(0);
  };

  if (isChallengeBoard && accessDenied) {
    return (
      <div className="community-page-layout">
        <CommunitySidebar activeBoardType={boardType} />
        <div className="community-list-container">
          <h2 className="community-list-title">{board.label}</h2>
          <div className="community-list-status">
            <p>이 챌린지를 진행 중이어야 게시판에 들어갈 수 있어요.</p>
            <button type="button" className="community-write-button" onClick={() => navigate("/challenge")}>
              챌린지 시작하러 가기
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="community-page-layout">
      <CommunitySidebar activeBoardType={boardType} />
      <div className="community-list-container">
        <div className="community-list-header">
          <h2 className="community-list-title">{board.label}</h2>
        </div>

        <form className="community-search-form" onSubmit={handleSearchSubmit}>
          <input
            type="text"
            className="community-search-input"
            placeholder="제목으로 검색"
            value={keywordInput}
            onChange={(e) => setKeywordInput(e.target.value)}
          />
          <button type="submit" className="community-search-button">
            🔍 검색
          </button>
        </form>

        <div className="community-sort-toggle">
          <button
            type="button"
            className={sortBy === "latest" ? "active" : ""}
            onClick={() => changeSortBy("latest")}
          >
            최신순
          </button>
          <button
            type="button"
            className={sortBy === "popular" ? "active" : ""}
            onClick={() => changeSortBy("popular")}
          >
            🔥 인기순
          </button>
        </div>

        {boardType === "FREE_TALK" && (
          <div className="community-prefix-filter">
            <select value={prefix} onChange={(e) => { setPrefix(e.target.value); setPage(0); }}>
              <option value="">전체</option>
              {FREE_TALK_PREFIXES.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>
        )}

        {loading && <p className="community-list-status">불러오는 중...</p>}
        {error && <p className="community-list-status error">{error}</p>}
        {!loading && !error && posts.length === 0 && (
          <p className="community-list-status">
            {keyword ? `"${keyword}"에 대한 검색 결과가 없어요.` : "아직 등록된 글이 없어요. 첫 글을 남겨보세요!"}
          </p>
        )}

        <div className="community-post-cards">
          {posts.map((post) => (
            <div
              key={post.postId}
              className="community-post-card"
              onClick={() => navigate(`/community/${post.postId}`)}
            >
              <div className="community-post-card-author">사용자 {post.userId}</div>
              <div className="community-post-card-body">
                <div className="community-post-card-title">
                  {post.prefix && <span className="community-post-card-prefix">{post.prefix}</span>}
                  {post.title}
                  {post.promotedRecipeId && <span className="community-post-card-badge">🏅 정식 레시피</span>}
                </div>
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

      <button type="button" className="community-write-fab" onClick={() => navigate(board.newPath)}>
        ✏️ 글쓰기
      </button>
    </div>
  );
}
