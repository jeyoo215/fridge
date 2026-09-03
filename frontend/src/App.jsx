import { useEffect, useState } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Nav from "./component/Nav";
import RequireAuth from "./component/RequireAuth";
import IngredientList from "./pages/IngredientList";
import IngredientRegisterForm from "./pages/IngredientRegisterForm";
import RecipeRecommend from "./pages/RecipeRecommend";
import RecipeDetail from "./pages/RecipeDetail";
import ShoppingList from "./pages/ShoppingList";
import MyShoppingList from "./pages/MyShoppingList";
import Challenge from "./pages/Challenge";
import CommunityList from "./pages/CommunityList";
import CommunityPostForm from "./pages/CommunityPostForm";
import CommunityPostDetail from "./pages/CommunityPostDetail";
import MyPage from "./pages/MyPage";
import Login from "./pages/Login";
import OAuthRedirect from "./pages/OAuthRedirect";
import FridgeDecorate from "./pages/FridgeDecorate";
import Admin from "./pages/Admin";
import AdminRecipeForm from "./pages/AdminRecipeForm";
import AdminReports from "./pages/AdminReports";
import {
  isLoggedIn,
  isAdmin,
  isSessionExpired,
  clearTokens,
  extendSessionIfActive,
} from "./api/authApi";
import "./App.css";

// "/" 경로는 RequireAuth로 안 감싸는 특수 라우트라(로그인 여부에 따라 다른 화면을 보여줘야 하니까)
function HomeRoute() {
  const [checked, setChecked] = useState(false);
  const [expired, setExpired] = useState(false);

  useEffect(() => {
    if (isLoggedIn() && isSessionExpired()) {
      clearTokens();
      setExpired(true);
    } else if (isLoggedIn()) {
      extendSessionIfActive();
    }
    setChecked(true);
  }, []);

  if (!checked) return null;
  if (expired) return <Navigate to="/login" replace state={{ expired: true }} />;
  if (!isLoggedIn()) return <Login />;
  if (isAdmin()) return <Admin />;
  return <IngredientList />;
}

function App() {
  return (
    <BrowserRouter>
      {isLoggedIn() && <Nav />}
      <Routes>
        <Route path="/" element={<HomeRoute />} />
        <Route path="/ingredients/new" element={<RequireAuth><IngredientRegisterForm /></RequireAuth>} />
        <Route path="/recipes" element={<RequireAuth><RecipeRecommend /></RequireAuth>} />
        <Route path="/recipes/:recipeId" element={<RequireAuth><RecipeDetail /></RequireAuth>} />
        <Route path="/recipes/:recipeId/shopping-list" element={<RequireAuth><ShoppingList /></RequireAuth>} />
        <Route path="/shopping-list" element={<RequireAuth><MyShoppingList /></RequireAuth>} />
        <Route path="/challenge" element={<RequireAuth><Challenge /></RequireAuth>} />
        {/* 커뮤니티 열람(레시피/잡담 게시판 목록, 게시글 상세)은 로그인 없이도 가능.
            글쓰기/수정/댓글/좋아요/스크랩 등 실제 활동은 각 화면에서 로그인 여부를 확인해서 막는다.
            챌린지 게시판은 "진행 중인 챌린지가 있어야" 들어갈 수 있어서 개념상 로그인이 필요하므로 그대로 막아둔다. */}
        <Route path="/community" element={<CommunityList key="RECIPE" boardType="RECIPE" />} />
        <Route path="/community/new" element={<RequireAuth><CommunityPostForm boardType="RECIPE" /></RequireAuth>} />
        <Route path="/community/challenge/fridge-clean" element={<RequireAuth><CommunityList key="CHALLENGE_FRIDGE_CLEAN" boardType="CHALLENGE_FRIDGE_CLEAN" /></RequireAuth>} />
        <Route path="/community/challenge/fridge-clean/new" element={<RequireAuth><CommunityPostForm boardType="CHALLENGE_FRIDGE_CLEAN" /></RequireAuth>} />
        <Route path="/community/challenge/target-ingredient" element={<RequireAuth><CommunityList key="CHALLENGE_TARGET_INGREDIENT" boardType="CHALLENGE_TARGET_INGREDIENT" /></RequireAuth>} />
        <Route path="/community/challenge/target-ingredient/new" element={<RequireAuth><CommunityPostForm boardType="CHALLENGE_TARGET_INGREDIENT" /></RequireAuth>} />
        <Route path="/community/free-talk" element={<CommunityList key="FREE_TALK" boardType="FREE_TALK" />} />
        <Route path="/community/free-talk/new" element={<RequireAuth><CommunityPostForm boardType="FREE_TALK" /></RequireAuth>} />
        <Route path="/community/:postId/edit" element={<RequireAuth><CommunityPostForm /></RequireAuth>} />
        <Route path="/community/:postId" element={<CommunityPostDetail />} />
        <Route path="/mypage" element={<RequireAuth><MyPage /></RequireAuth>} />
        <Route path="/login" element={<Login />} />
        <Route path="/oauth/redirect" element={<OAuthRedirect />} />
        <Route path="/fridge" element={<FridgeDecorate />} />
        <Route path="/admin" element={isAdmin() ? <Admin /> : <Navigate to="/" replace />} />
        <Route path="/admin/recipes/new" element={isAdmin() ? <AdminRecipeForm /> : <Navigate to="/" replace />} />
        <Route path="/admin/reports" element={isAdmin() ? <AdminReports /> : <Navigate to="/" replace />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;