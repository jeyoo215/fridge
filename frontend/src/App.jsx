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
import Stats from "./pages/Stats";
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
        <Route path="/stats" element={<Stats />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;