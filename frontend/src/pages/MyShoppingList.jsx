import { useEffect, useState } from "react";
import {
  fetchMyShoppingList,
  checkShoppingItem,
  uncheckShoppingItem,
  deleteShoppingItem,
  addManualShoppingItem,
} from "../api/shoppingListApi";
import { searchIngredients } from "../api/ingredientApi";
import "./MyShoppingList.css";

const TEMP_USER_ID = 1;

export default function MyShoppingList() {
  const [list, setList] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 셀프 추가 관련 상태
  const [keyword, setKeyword] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [selectedIngredient, setSelectedIngredient] = useState(null);
  const [quantity, setQuantity] = useState("");
  const [unit, setUnit] = useState("");
  const [adding, setAdding] = useState(false);

  const loadList = () => {
    fetchMyShoppingList(TEMP_USER_ID)
      .then(setList)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadList();
  }, []);

  // 검색어 입력 300ms 후 자동완성
  useEffect(() => {
    if (!keyword || selectedIngredient) {
      setSearchResults([]);
      return;
    }
    const timer = setTimeout(() => {
      searchIngredients(keyword).then(setSearchResults).catch(() => {});
    }, 300);
    return () => clearTimeout(timer);
  }, [keyword, selectedIngredient]);

  const handleSelectIngredient = (ingredient) => {
    setSelectedIngredient(ingredient);
    setKeyword(ingredient.ingredientName);
    setSearchResults([]);
  };

  const handleAddManualItem = async () => {
    if (!selectedIngredient) return;
    setAdding(true);
    try {
      await addManualShoppingItem(TEMP_USER_ID, {
        ingredientId: selectedIngredient.ingredientId,
        quantity: quantity ? Number(quantity) : null,
        unit: unit || null,
      });
      setSelectedIngredient(null);
      setKeyword("");
      setQuantity("");
      setUnit("");
      loadList();
    } catch (err) {
      setError(err.message);
    } finally {
      setAdding(false);
    }
  };

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

  const items = list?.items ?? [];

  return (
    <div className="my-shopping-list-container">
      <h2 className="my-shopping-list-title">🛒 내 장보기 리스트</h2>

      <div className="my-shopping-list-add-form">
        <div className="my-shopping-list-add-search">
          <input
            type="text"
            placeholder="재료 이름으로 검색"
            value={keyword}
            onChange={(e) => {
              setKeyword(e.target.value);
              setSelectedIngredient(null);
            }}
          />
          {searchResults.length > 0 && (
            <ul className="my-shopping-list-search-results">
              {searchResults.map((ingredient) => (
                <li key={ingredient.ingredientId} onClick={() => handleSelectIngredient(ingredient)}>
                  {ingredient.ingredientName}
                </li>
              ))}
            </ul>
          )}
        </div>
        <input
          type="number"
          placeholder="수량"
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
          className="my-shopping-list-add-quantity"
        />
        <input
          type="text"
          placeholder="단위"
          value={unit}
          onChange={(e) => setUnit(e.target.value)}
          className="my-shopping-list-add-unit"
        />
        <button onClick={handleAddManualItem} disabled={!selectedIngredient || adding}>
          {adding ? "담는 중..." : "+ 추가"}
        </button>
      </div>

      {error && <p className="my-shopping-list-status">{error}</p>}

      {items.length === 0 ? (
        <p className="my-shopping-list-status">
          아직 담긴 재료가 없어요. 위에서 검색해서 담거나, 레시피 상세에서 부족한 재료를 담아보세요!
        </p>
      ) : (
        <ul className="my-shopping-list-items">
          {items.map((item) => (
            <li key={item.itemId} className={`my-shopping-list-item${item.checked ? " checked" : ""}`}>
              <label className="my-shopping-list-item-label">
                <input type="checkbox" checked={item.checked} onChange={() => handleToggleCheck(item)} />
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