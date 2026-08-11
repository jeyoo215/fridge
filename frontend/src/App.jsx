import { BrowserRouter, Routes, Route } from "react-router-dom";
import Nav from "./component/Nav";
import IngredientList from "./pages/IngredientList";
import IngredientRegisterForm from "./pages/IngredientRegisterForm";
import RecipeRecommend from "./pages/RecipeRecommend";
import RecipeDetail from "./pages/RecipeDetail";
import ShoppingList from "./pages/ShoppingList";
import MyShoppingList from "./pages/MyShoppingList";
import Challenge from "./pages/Challenge";
import PopularRecipes from "./pages/PopularRecipes";
import CommunityList from "./pages/CommunityList";
import CommunityPostForm from "./pages/CommunityPostForm";
import CommunityPostDetail from "./pages/CommunityPostDetail";
import "./App.css";

function App() {
  return (
    <BrowserRouter>
      <Nav />
      <Routes>
        <Route path="/" element={<IngredientList />} />
        <Route path="/ingredients/new" element={<IngredientRegisterForm />} />
        <Route path="/recipes" element={<RecipeRecommend />} />
        <Route path="/recipes/popular" element={<PopularRecipes />} />
        <Route path="/recipes/:recipeId" element={<RecipeDetail />} />
        <Route path="/recipes/:recipeId/shopping-list" element={<ShoppingList />} />
        <Route path="/shopping-list" element={<MyShoppingList />} />
        <Route path="/challenge" element={<Challenge />} />
        <Route path="/community" element={<CommunityList />} />
        <Route path="/community/new" element={<CommunityPostForm />} />
        <Route path="/community/:postId/edit" element={<CommunityPostForm />} />
        <Route path="/community/:postId" element={<CommunityPostDetail />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;