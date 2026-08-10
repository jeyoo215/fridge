import { useEffect, useState } from "react";
import {
  fetchMyShoppingList,
  checkShoppingItem,
  uncheckShoppingItem,
  deleteShoppingItem,
} from "../api/shoppingListApi";
import "./MyShoppingList.css";

const TEMP_USER_ID = 1;

export default function MyShoppingList() {
  const [list, setList] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadList = () => {
    fetchMyShoppingList(TEMP_USER_ID)
      .then(setList)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadList();
  }, []);

  const handleToggleCheck = async (item) => {
    try {
      if (item.checked) {
        await uncheckShoppingItem(TEMP_USER_ID, item.itemId);
      } else {
        await checkShoppingItem(TEMP_USER_ID, item.itemId);
      }
      loadList();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDelete = async (item) => {
    try {
      await deleteShoppingItem(TEMP_USER_ID, item.itemId);
      loadList();
    } catch (err) {
      setError(err.message);
    }
  };

  if (loading) return <p className="my-shopping-list-status">불러오는 중...</p>;
  if (error) return <p className="my-shopping-list-status">{error}</p>;

  const items = list?.items ?? [];

  return (
    <div className="my-shopping-list-container">
      <h2 className="my-shopping-list-title">🛒 내 장보기 리스트</h2>

      {items.length === 0 ? (
        <p className="my-shopping-list-status">
          아직 담긴 재료가 없어요. 레시피 상세에서 부족한 재료를 담아보세요!
        </p>
      ) : (
        <ul className="my-shopping-list-items">
          {items.map((item) => (
            <li key={item.itemId} className={`my-shopping-list-item${item.checked ? " checked" : ""}`}>
              <label className="my-shopping-list-item-label">
                <input
                  type="checkbox"
                  checked={item.checked}
                  onChange={() => handleToggleCheck(item)}
                />
                <span className="my-shopping-list-item-name">{item.ingredientName}</span>
                <span className="my-shopping-list-item-amount">
                  {item.quantity} {item.unit}
                </span>
              </label>
              <button className="my-shopping-list-item-delete" onClick={() => handleDelete(item)}>
                ✕
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}