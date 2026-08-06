import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { fetchShoppingList } from "../api/shoppingListApi";
import "./ShoppingList.css";

const TEMP_USER_ID = 1;

export default function ShoppingList() {
  const { recipeId } = useParams();
  const navigate = useNavigate();
  const [list, setList] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchShoppingList(TEMP_USER_ID, recipeId)
      .then(setList)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [recipeId]);

  if (loading) return <p className="shopping-list-status">불러오는 중...</p>;
  if (error) return <p className="shopping-list-status">{error}</p>;
  if (!list) return null;

  return (
    <div className="shopping-list-container">
      <button className="shopping-list-back" onClick={() => navigate(-1)}>
        ← 뒤로
      </button>

      <h2 className="shopping-list-title">{list.recipeName} 장보기 리스트</h2>

      {list.missingIngredients.length === 0 ? (
        <p className="shopping-list-status">필요한 재료를 다 갖고 있어요! 🎉</p>
      ) : (
        <ul className="shopping-list-items">
          {list.missingIngredients.map((item) => (
            <li key={item.ingredientId} className="shopping-list-item">
              <span className="shopping-list-item-name">{item.ingredientName}</span>
              <span className="shopping-list-item-amount">
                {item.quantity} {item.unit}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}