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
import { isLoggedIn } from "./api/authApi";
import FridgeDecorate from "./pages/FridgeDecorate";
import "./App.css";

function App() {
  return (
    <BrowserRouter>
      {isLoggedIn() && <Nav />}
      <Routes>
        <Route path="/" element={isLoggedIn() ? <IngredientList /> : <Login />} />
        <Route path="/ingredients/new" element={<RequireAuth><IngredientRegisterForm /></RequireAuth>} />
        <Route path="/recipes" element={<RequireAuth><RecipeRecommend /></RequireAuth>} />
        <Route path="/recipes/:recipeId" element={<RequireAuth><RecipeDetail /></RequireAuth>} />
        <Route path="/recipes/:recipeId/shopping-list" element={<RequireAuth><ShoppingList /></RequireAuth>} />
        <Route path="/shopping-list" element={<RequireAuth><MyShoppingList /></RequireAuth>} />
        <Route path="/challenge" element={<RequireAuth><Challenge /></RequireAuth>} />
        <Route path="/community" element={<RequireAuth><CommunityList key="RECIPE" boardType="RECIPE" /></RequireAuth>} />
        <Route path="/community/new" element={<RequireAuth><CommunityPostForm boardType="RECIPE" /></RequireAuth>} />
        <Route path="/community/challenge/fridge-clean" element={<RequireAuth><CommunityList key="CHALLENGE_FRIDGE_CLEAN" boardType="CHALLENGE_FRIDGE_CLEAN" /></RequireAuth>} />
        <Route path="/community/challenge/fridge-clean/new" element={<RequireAuth><CommunityPostForm boardType="CHALLENGE_FRIDGE_CLEAN" /></RequireAuth>} />
        <Route path="/community/challenge/target-ingredient" element={<RequireAuth><CommunityList key="CHALLENGE_TARGET_INGREDIENT" boardType="CHALLENGE_TARGET_INGREDIENT" /></RequireAuth>} />
        <Route path="/community/challenge/target-ingredient/new" element={<RequireAuth><CommunityPostForm boardType="CHALLENGE_TARGET_INGREDIENT" /></RequireAuth>} />
        <Route path="/community/free-talk" element={<RequireAuth><CommunityList key="FREE_TALK" boardType="FREE_TALK" /></RequireAuth>} />
        <Route path="/community/free-talk/new" element={<RequireAuth><CommunityPostForm boardType="FREE_TALK" /></RequireAuth>} />
        <Route path="/community/:postId/edit" element={<RequireAuth><CommunityPostForm /></RequireAuth>} />
        <Route path="/community/:postId" element={<RequireAuth><CommunityPostDetail /></RequireAuth>} />
        <Route path="/mypage" element={<RequireAuth><MyPage /></RequireAuth>} />
        <Route path="/login" element={<Login />} />
        <Route path="/oauth/redirect" element={<OAuthRedirect />} />
        <Route path="*" element={<Navigate to="/" replace />} />
        <Route path="/fridge" element={<RequireAuth><FridgeDecorate /></RequireAuth>} />
        <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    </BrowserRouter>
  );
}

export default App;