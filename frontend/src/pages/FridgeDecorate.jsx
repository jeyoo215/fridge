import { useEffect, useRef, useState } from "react";
import {
  fetchFridgeItems,
  moveFridgeItem,
  removeFridgeItem,
} from "../api/fridgeApi";
import "./FridgeDecorate.css";

// 좌표 비율 기준 구역 판정 (상단 40% = 냉동, 나머지 = 냉장)
const FROZEN_MAX_Y = 0.4;

function zoneByY(posY) {
  return posY <= FROZEN_MAX_Y ? "FROZEN" : "FRIDGE";
}

// 유통기한 D-day 계산
function dDay(expirationDate) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const exp = new Date(expirationDate);
  const diff = Math.round((exp - today) / 86400000);
  return diff;
}

export default function FridgeDecorate() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");
  const boardRef = useRef(null);
  const dragging = useRef(null); // { id, offsetX, offsetY }

  useEffect(() => {
    fetchFridgeItems()
      .then(setItems)
      .catch((e) => setError(e.message));
  }, []);

  function onPointerDown(e, item) {
    e.preventDefault();
    dragging.current = { id: item.fridgeItemId };
  }

  function onPointerMove(e) {
    if (!dragging.current) return;
    const rect = boardRef.current.getBoundingClientRect();
    let x = (e.clientX - rect.left) / rect.width;
    let y = (e.clientY - rect.top) / rect.height;
    x = Math.min(1, Math.max(0, x));
    y = Math.min(1, Math.max(0, y));
    setItems((prev) =>
      prev.map((it) =>
        it.fridgeItemId === dragging.current.id ? { ...it, posX: x, posY: y } : it
      )
    );
  }

  async function onPointerUp() {
    const drag = dragging.current;
    dragging.current = null;
    if (!drag) return;
    const item = items.find((it) => it.fridgeItemId === drag.id);
    if (!item) return;
    const zone = zoneByY(item.posY);
    try {
      await moveFridgeItem(item.fridgeItemId, item.posX, item.posY, zone);
      setItems((prev) =>
        prev.map((it) => (it.fridgeItemId === item.fridgeItemId ? { ...it, zone } : it))
      );
    } catch (e) {
      setError(e.message);
    }
  }

  async function handleRemove(id) {
    try {
      await removeFridgeItem(id);
      setItems((prev) => prev.filter((it) => it.fridgeItemId !== id));
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <div className="fridge-decorate">
      <h2>내 냉장고</h2>
      {error && <p className="fridge-error">{error}</p>}

      <div
        className="fridge-board"
        ref={boardRef}
        onPointerMove={onPointerMove}
        onPointerUp={onPointerUp}
        onPointerLeave={onPointerUp}
      >
        <div className="fridge-zone-frozen">냉동</div>
        <div className="fridge-zone-fridge">냉장</div>

        {items.map((item) => {
          const d = dDay(item.expirationDate);
          const urgent = d <= 3;
          return (
            <div
              key={item.fridgeItemId}
              className="fridge-item"
              style={{ left: `${item.posX * 100}%`, top: `${item.posY * 100}%` }}
              onPointerDown={(e) => onPointerDown(e, item)}
            >
              {item.imageUrl && <img src={item.imageUrl} alt={item.ingredientName} />}
              <span className="fridge-item-name">{item.ingredientName}</span>
              {urgent && (
                <span className="fridge-item-dday">
                  {d < 0 ? "만료" : `D-${d}`}
                </span>
              )}
              <button
                className="fridge-item-remove"
                onClick={() => handleRemove(item.fridgeItemId)}
              >
                ×
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}