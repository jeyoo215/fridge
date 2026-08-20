import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Nav from "./component/Nav";
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
import "./App.css";

function App() {
  return (
    <BrowserRouter>
      <Nav />
      <Routes>
        <Route path="/" element={<IngredientList />} />
        <Route path="/ingredients/new" element={<IngredientRegisterForm />} />
        <Route path="/recipes" element={<RecipeRecommend />} />
        <Route path="/recipes/:recipeId" element={<RecipeDetail />} />
        <Route path="/recipes/:recipeId/shopping-list" element={<ShoppingList />} />
        <Route path="/shopping-list" element={<MyShoppingList />} />
        <Route path="/challenge" element={<Challenge />} />
        <Route path="/community" element={<CommunityList key="RECIPE" boardType="RECIPE" />} />
        <Route path="/community/new" element={<CommunityPostForm boardType="RECIPE" />} />
        <Route path="/community/challenge/fridge-clean" element={<CommunityList key="CHALLENGE_FRIDGE_CLEAN" boardType="CHALLENGE_FRIDGE_CLEAN" />} />
        <Route path="/community/challenge/fridge-clean/new" element={<CommunityPostForm boardType="CHALLENGE_FRIDGE_CLEAN" />} />
        <Route path="/community/challenge/target-ingredient" element={<CommunityList key="CHALLENGE_TARGET_INGREDIENT" boardType="CHALLENGE_TARGET_INGREDIENT" />} />
        <Route path="/community/challenge/target-ingredient/new" element={<CommunityPostForm boardType="CHALLENGE_TARGET_INGREDIENT" />} />
        <Route path="/community/free-talk" element={<CommunityList key="FREE_TALK" boardType="FREE_TALK" />} />
        <Route path="/community/free-talk/new" element={<CommunityPostForm boardType="FREE_TALK" />} />
        <Route path="/community/:postId/edit" element={<CommunityPostForm />} />
        <Route path="/community/:postId" element={<CommunityPostDetail />} />
        <Route path="/mypage" element={<MyPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/oauth/redirect" element={<OAuthRedirect />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;