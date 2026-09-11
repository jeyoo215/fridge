import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { fetchSharedShoppingList, toggleSharedShoppingItem } from "../api/shoppingListApi";
import "./MyShoppingList.css";

export default function SharedShoppingList() {
  const { shareToken } = useParams();
  const [list, setList] = useState(null);
  const [error, setError] = useState(null);

  const loadList = () => {
    fetchSharedShoppingList(shareToken).then(setList).catch((err) => setError(err.message));
  };

  useEffect(() => {
    loadList();
  }, [shareToken]);

  const handleToggle = async (item) => {
    try {
      await toggleSharedShoppingItem(shareToken, item.itemId);
      loadList();
    } catch (err) {
      setError(err.message);
    }
  };

  if (error) return <p className="my-shopping-list-status">{error}</p>;
  if (!list) return <p className="my-shopping-list-status">불러오는 중...</p>;

  return (
    <div className="my-shopping-list-container">
      <h2 className="my-shopping-list-title">🛒 공유받은 장보기 리스트</h2>
      <p className="my-shopping-list-status">체크만 같이 할 수 있어요. 추가/삭제는 원본 리스트에서만 가능해요.</p>
      <ul className="my-shopping-list-items">
        {list.items.map((item) => (
          <li key={item.itemId} className={`my-shopping-list-item${item.checked ? " checked" : ""}`}>
            <label className="my-shopping-list-item-label">
              <input type="checkbox" checked={item.checked} onChange={() => handleToggle(item)} />
              <span className="my-shopping-list-item-name">{item.ingredientName}</span>
            </label>
            {item.quantity != null && (
              <span className="my-shopping-list-item-unit">{item.quantity} {item.unit}</span>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}