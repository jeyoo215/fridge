import { useState } from "react";
import IngredientList from "./pages/IngredientList";
import IngredientRegisterForm from "./pages/IngredientRegisterForm";
import "./App.css";

// TODO: 화면이 더 늘어나면 react-router로 교체하기.
// 지금은 "목록 <-> 등록" 두 화면만 있어서 상태값으로 간단히 전환함.
function App() {
  const [view, setView] = useState("list"); // "list" | "register"
  const [refreshKey, setRefreshKey] = useState(0); // 등록 완료 후 목록 다시 불러오기용

  if (view === "register") {
    return (
      <IngredientRegisterForm
        onCancel={() => setView("list")}
        onRegistered={() => {
          setRefreshKey((k) => k + 1);
          setView("list");
        }}
      />
    );
  }

  return <IngredientList key={refreshKey} onAddClick={() => setView("register")} />;
}

export default App;
