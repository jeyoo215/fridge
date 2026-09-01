import { useEffect, useRef, useState } from "react";
import FridgeAddModal from "../component/FridgeAddModal";
import {
  fetchFridgeItems,
  moveFridgeItem,
  removeFridgeItem,
  resizeFridgeItem,
} from "../api/fridgeApi";
import "./FridgeDecorate.css";

const MEDIA_BASE = `http://${window.location.hostname}:8080`;

function toImageSrc(url) {
  if (!url) return "";
  return url.startsWith("http") ? url : `${MEDIA_BASE}${url}`;
}

const FROZEN_MAX_Y = 0.4;
const MIN_SCALE = 0.5;
const MAX_SCALE = 2.5;
const SCALE_STEP = 0.2;

function zoneByY(posY) {
  return posY <= FROZEN_MAX_Y ? "FROZEN" : "FRIDGE";
}

function dDay(expirationDate) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const exp = new Date(expirationDate);
  return Math.round((exp - today) / 86400000);
}

export default function FridgeDecorate() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");
  const [showAdd, setShowAdd] = useState(false);
  const [selectedId, setSelectedId] = useState(null);   // 원클릭 선택
  const [editingId, setEditingId] = useState(null);      // 편집모드
  const boardRef = useRef(null);
  const dragging = useRef(false);

  useEffect(() => {
    fetchFridgeItems().then(setItems).catch((e) => setError(e.message));
  }, []);

  // 편집모드에서만 드래그
  function onPointerDown(e, item) {
    if (editingId !== item.fridgeItemId) return;
    e.preventDefault();
    dragging.current = true;
  }

  function onPointerMove(e) {
    if (!dragging.current || editingId == null) return;
    const rect = boardRef.current.getBoundingClientRect();
    let x = (e.clientX - rect.left) / rect.width;
    let y = (e.clientY - rect.top) / rect.height;
    x = Math.min(1, Math.max(0, x));
    y = Math.min(1, Math.max(0, y));
    setItems((prev) =>
      prev.map((it) => (it.fridgeItemId === editingId ? { ...it, posX: x, posY: y } : it))
    );
  }

  function onPointerUp() {
    dragging.current = false;
  }

  // 원클릭 = 선택
  function handleSelect(e, item) {
    e.stopPropagation();
    if (editingId === item.fridgeItemId) return; // 편집중이면 선택 유지
    setSelectedId(item.fridgeItemId);
  }

  // 빈 공간 클릭 = 선택·편집 해제
  function handleBoardClick() {
    setSelectedId(null);
    setEditingId(null);
  }

  function enterEdit(e, id) {
    e.stopPropagation();
    setEditingId(id);
    setSelectedId(id);
  }

  function changeScale(e, id, delta) {
    e.stopPropagation();
    setItems((prev) =>
      prev.map((it) => {
        if (it.fridgeItemId !== id) return it;
        const cur = it.scale || 1;
        const next = Math.min(MAX_SCALE, Math.max(MIN_SCALE, cur + delta));
        return { ...it, scale: next };
      })
    );
  }

  // 적용 = 위치·크기 저장 + 편집모드 종료
  async function applyEdit(e, id) {
    e.stopPropagation();
    const item = items.find((it) => it.fridgeItemId === id);
    if (!item) return;
    const zone = zoneByY(item.posY);
    try {
      await moveFridgeItem(item.fridgeItemId, item.posX, item.posY, zone);
      await resizeFridgeItem(item.fridgeItemId, item.scale || 1);
      setItems((prev) =>
        prev.map((it) => (it.fridgeItemId === id ? { ...it, zone } : it))
      );
      setEditingId(null);
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleRemove(e, id) {
    e.stopPropagation();
    try {
      await removeFridgeItem(id);
      setItems((prev) => prev.filter((it) => it.fridgeItemId !== id));
      setSelectedId(null);
      setEditingId(null);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="fridge-decorate">
      <h2>내 냉장고</h2>
      {error && <p className="fridge-error">{error}</p>}

      <button className="fridge-add-button" onClick={() => setShowAdd(true)}>
        + 재료 추가
      </button>

      {showAdd && (
        <FridgeAddModal
          onClose={() => setShowAdd(false)}
          onAdded={() => fetchFridgeItems().then(setItems).catch((e) => setError(e.message))}
        />
      )}

      <div
        className="fridge-board"
        ref={boardRef}
        onPointerMove={onPointerMove}
        onPointerUp={onPointerUp}
        onPointerLeave={onPointerUp}
        onClick={handleBoardClick}
      >
        <div className="fridge-zone-frozen">❄️냉동</div>
        <div className="fridge-zone-fridge">🧊냉장</div>

        {items.map((item) => {
          const d = dDay(item.expirationDate);
          const urgent = d <= 3;
          const selected = selectedId === item.fridgeItemId;
          const editing = editingId === item.fridgeItemId;
          const scale = item.scale || 1;
          return (
            <div
              key={item.fridgeItemId}
              className={`fridge-item ${editing ? "editing" : ""}`}
              style={{
                left: `${item.posX * 100}%`,
                top: `${item.posY * 100}%`,
                transform: `translate(-50%, -50%) scale(${scale})`,
                cursor: editing ? "grab" : "pointer",
              }}
              onPointerDown={(e) => onPointerDown(e, item)}
              onClick={(e) => handleSelect(e, item)}
            >
              {item.imageType === "SYSTEM" ? (
                <span className="fridge-item-emoji">{item.imageUrl}</span>
              ) : (
                item.imageUrl && <img src={toImageSrc(item.imageUrl)} alt={item.ingredientName} />
              )}

              {urgent && (
                <span className="fridge-item-dday">{d < 0 ? "만료" : `D-${d}`}</span>
              )}

              {/* 선택 상태: 편집 버튼 + x */}
              {selected && !editing && (
                <>
                  <button
                    className="fridge-item-edit"
                    style={{ transform: `translateX(-50%) scale(${1 / scale})` }}
                    onClick={(e) => enterEdit(e, item.fridgeItemId)}
                  >
                    편집
                  </button>
                  <button
                    className="fridge-item-remove"
                    style={{ transform: `scale(${1 / scale})` }}
                    onClick={(e) => handleRemove(e, item.fridgeItemId)}
                  >
                    ×
                  </button>
                </>
              )}

              {/* 편집모드: 크기조절 + 적용 */}
              {editing && (
                <div
                  className="fridge-item-controls"
                  style={{ transform: `translateX(-50%) scale(${1 / scale})` }}
                  onPointerDown={(e) => e.stopPropagation()}
                >
                  <button onClick={(e) => changeScale(e, item.fridgeItemId, -SCALE_STEP)}>−</button>
                  <button onClick={(e) => changeScale(e, item.fridgeItemId, SCALE_STEP)}>+</button>
                  <button className="fridge-item-apply" onClick={(e) => applyEdit(e, item.fridgeItemId)}>
                    적용
                  </button>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}