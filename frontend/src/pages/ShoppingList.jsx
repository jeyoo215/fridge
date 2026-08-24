import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { fetchShoppingList, addMissingIngredientsToMyList } from "../api/shoppingListApi";
import "./ShoppingList.css";


export default function ShoppingList() {
  const { recipeId } = useParams();
  const navigate = useNavigate();
  const [list, setList] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    fetchShoppingList(recipeId)
      .then((data) => {
        setList(data);
        // 이미 전부 담겨있으면 처음부터 '담음' 상태로 표시
        if (data.missingIngredients.length > 0 && data.missingIngredients.every((i) => i.inMyList)) {
          setAdded(true);
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [recipeId]);

  const handleAddToMyList = async () => {
    setAdding(true);
    try {
      await addMissingIngredientsToMyList(recipeId);
      setAdded(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setAdding(false);
    }
  };

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
        <>
          <ul className="shopping-list-items">
            {list.missingIngredients.map((item) => (
              <li key={item.ingredientId} className="shopping-list-item">
                <span className="shopping-list-item-name">
                  {item.ingredientName}
                  {item.inMyList && <span className="shopping-list-item-check"> ✓ 담음</span>}
                </span>
                <span className="shopping-list-item-amount">
                  {item.quantity} {item.unit}
                </span>
              </li>
            ))}
          </ul>

          <button
            className="shopping-list-add-button"
            onClick={handleAddToMyList}
            disabled={adding || added}
          >
            {added ? "✓ 내 장보기 리스트에 담았어요" : adding ? "담는 중..." : "내 장보기 리스트에 담기"}
          </button>
        </>
      )}
    </div>
  );
}