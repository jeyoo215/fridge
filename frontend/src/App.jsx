import IngredientList from "./pages/IngredientList";
import "./App.css";

// TODO: 다른 화면(로그인, 레시피 추천 등)이 추가되면 react-router로 교체하기.
// 지금은 재료 관리 화면 개발/테스트를 위해 바로 렌더링만 함.
function App() {
  return <IngredientList />;
}

export default App;