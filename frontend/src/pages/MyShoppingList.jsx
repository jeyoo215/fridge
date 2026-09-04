import { useEffect, useRef, useState } from "react";
import {
  fetchMyShoppingList,
  checkShoppingItem,
  uncheckShoppingItem,
  deleteShoppingItem,
  addManualShoppingItem,
  reorderShoppingItems,
  deleteCheckedShoppingItems,
  deleteAllShoppingItems,
  updateShoppingItemQuantity,
  setAllShoppingItemsChecked,
  purchaseCheckedShoppingItems,
} from "../api/shoppingListApi";
import { fetchActiveChallenge } from "../api/challengeApi";
import { searchIngredients } from "../api/ingredientApi";
import "./MyShoppingList.css";

export default function MyShoppingList() {
  const [list, setList] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [keyword, setKeyword] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [selectedIngredient, setSelectedIngredient] = useState(null);
  const [quantity, setQuantity] = useState("");
  const [unit, setUnit] = useState("");
  const [adding, setAdding] = useState(false);

  const dragIndexRef = useRef(null);

  const loadList = () => {
    fetchMyShoppingList()
      .then(setList)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  const [activeChallenge, setActiveChallenge] = useState(null);

  useEffect(() => {
    fetchActiveChallenge().then(setActiveChallenge).catch(() => {});
  }, []);

  useEffect(() => {
    loadList();
  }, []);

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
      await addManualShoppingItem({
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
        await uncheckShoppingItem(item.itemId);
      } else {
        await checkShoppingItem(item.itemId);
      }
      loadList();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDelete = async (item) => {
    try {
      await deleteShoppingItem(item.itemId);
      loadList();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteChecked = async () => {
    try {
      await deleteCheckedShoppingItems();
      loadList();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleQuantityChange = async (item, delta) => {
    const current = item.quantity ?? 0;
    const next = current + delta;
    if (next <= 0) return;
    try {
      await updateShoppingItemQuantity(item.itemId, next);
      loadList();
    } catch (err) {
      setError(err.message);
    }
  };

  // 드래그 정렬
  const handleDragStart = (index) => {
    dragIndexRef.current = index;
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleDrop = async (dropIndex) => {
    const dragIndex = dragIndexRef.current;
    dragIndexRef.current = null;
    if (dragIndex === null || dragIndex === dropIndex) return;

    const reordered = [...items];
    const [moved] = reordered.splice(dragIndex, 1);
    reordered.splice(dropIndex, 0, moved);

    setList({ ...list, items: reordered }); // 낙관적 업데이트

    try {
      await reorderShoppingItems(reordered.map((i) => i.itemId));
    } catch (err) {
      setError(err.message);
      loadList(); // 실패 시 서버 상태로 복구
    }
  };

  if (loading) return <p className="my-shopping-list-status">불러오는 중...</p>;

  const items = list?.items ?? [];
  const hasChecked = items.some((i) => i.checked);

  const allChecked = items.length > 0 && items.every((i) => i.checked);

  const handleToggleAllChecked = async () => {
    try {
      await setAllShoppingItemsChecked(!allChecked);
      loadList();
    } catch (err) {
      setError(err.message);
    }
  };

  const handlePurchaseChecked = async () => {
    if (activeChallenge?.status === "진행중" && activeChallenge.type === "FRIDGE_CLEAN") {
      const confirmed = window.confirm(
        "냉장고 파먹기 챌린지 진행 중입니다.\n정말로 구매하시겠습니까? 챌린지가 실패 처리될 수 있어요."
      );
      if (!confirmed) return;
    }

    try {
      const result = await purchaseCheckedShoppingItems();
      loadList();
      alert(`${result.createdUserIngredientIds.length}개 재료를 냉장고에 담았어요!`);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="my-shopping-list-container">
      <div className="my-shopping-list-header">
        <h2 className="my-shopping-list-title">🛒 내 장보기 리스트</h2>
        {items.length > 0 && (
          <div className="my-shopping-list-actions-row">
            <label className="my-shopping-list-select-all">
              <input type="checkbox" checked={allChecked} onChange={handleToggleAllChecked} />
              전체선택
            </label>
            <div className="my-shopping-list-bulk-actions">
              <button onClick={handlePurchaseChecked} disabled={!hasChecked} className="primary">
                구매
              </button>
              <button onClick={handleDeleteChecked} disabled={!hasChecked}>
                삭제
              </button>
            </div>
          </div>
        )}
      </div>

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
          {items.map((item, index) => (
            <li
              key={item.itemId}
              className={`my-shopping-list-item${item.checked ? " checked" : ""}`}
              draggable
              onDragStart={() => handleDragStart(index)}
              onDragOver={handleDragOver}
              onDrop={() => handleDrop(index)}
            >
              <span className="my-shopping-list-item-handle">⠿</span>
              <label className="my-shopping-list-item-label">
                <input type="checkbox" checked={item.checked} onChange={() => handleToggleCheck(item)} />
                <span className="my-shopping-list-item-name">{item.ingredientName}</span>
              </label>

              {item.quantity != null && (
                <span className="my-shopping-list-item-amount">
                  <button onClick={() => handleQuantityChange(item, -1)}>-</button>
                  <span className="my-shopping-list-item-quantity">{item.quantity}</span>
                  <button onClick={() => handleQuantityChange(item, 1)}>+</button>
                  {item.unit && <span className="my-shopping-list-item-unit">{item.unit}</span>}
                </span>
              )}

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