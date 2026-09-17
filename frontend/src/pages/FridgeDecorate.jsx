import { useEffect, useRef, useState } from "react";
import FridgeAddModal from "../component/FridgeAddModal";
import { HOST } from "../api/config";
import {
  fetchFridgeItems,
  moveFridgeItem,
  removeFridgeItem,
  resizeFridgeItem,
  changeFridgeItemImage
} from "../api/fridgeApi";
import "./FridgeDecorate.css";
import { deleteIngredient } from "../api/ingredientApi";
import { rotateFridgeItem } from "../api/fridgeApi";

function toImageSrc(url) {
  if (!url) return "";
  return url.startsWith("http") ? url : `${HOST}${url}`;
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
  const [removeTarget, setRemoveTarget] = useState(null); // 삭제 확인 대상 item
  const [showLabels, setShowLabels] = useState(false);
  const [imageEditId, setImageEditId] = useState(null);
  const interaction = useRef(null); // 회전
  const boardRef = useRef(null);
  const dragging = useRef(false);

  useEffect(() => {
    fetchFridgeItems().then(setItems).catch((e) => setError(e.message));
  }, []);

  // 편집모드에서만 드래그
  function onPointerDown(e, item) {
    if (selectedId !== item.fridgeItemId) return;
    e.preventDefault();
    dragging.current = true;
  }

  function onPointerMove(e) {
    // 리사이즈/회전 우선
    if (interaction.current) {
      const it = interaction.current;
      if (it.type === "resize") {
        const dist = Math.hypot(e.clientX - it.centerX, e.clientY - it.centerY);
        let next = it.startScale * (dist / it.startDist);
        next = Math.min(MAX_SCALE, Math.max(MIN_SCALE, next));
        setItems((prev) => prev.map((x) => x.fridgeItemId === it.id ? { ...x, scale: next } : x));
      } else if (it.type === "rotate") {
        const angle = Math.atan2(e.clientY - it.centerY, e.clientX - it.centerX);
        const deg = it.startRotation + (angle - it.startAngle) * (180 / Math.PI);
        setItems((prev) => prev.map((x) => x.fridgeItemId === it.id ? { ...x, rotation: deg } : x));
      }
      return;
    }
    // 위치 드래그
    if (!dragging.current || selectedId == null) return;
    const rect = boardRef.current.getBoundingClientRect();
    let x = (e.clientX - rect.left) / rect.width;
    let y = (e.clientY - rect.top) / rect.height;
    x = Math.min(1, Math.max(0, x));
    y = Math.min(1, Math.max(0, y));
    setItems((prev) => prev.map((it) => it.fridgeItemId === selectedId ? { ...it, posX: x, posY: y } : it));
  }

  async function onPointerUp() {
    const wasDragging = dragging.current;
    const it = interaction.current;
    dragging.current = false;
    interaction.current = null;

    const targetId = it ? it.id : (wasDragging ? selectedId : null);
    if (targetId == null) return;

    const item = items.find((x) => x.fridgeItemId === targetId);
    if (!item) return;

    try {
      if (it?.type === "resize") {
        await resizeFridgeItem(item.fridgeItemId, item.scale || 1);
      } else if (it?.type === "rotate") {
        await rotateFridgeItem(item.fridgeItemId, item.rotation || 0);
      } else {
        const zone = zoneByY(item.posY);
        await moveFridgeItem(item.fridgeItemId, item.posX, item.posY, zone);
        setItems((prev) =>
          prev.map((x) => (x.fridgeItemId === targetId ? { ...x, zone } : x))
        );
      }
    } catch (err) {
      setError(err.message);
    }
  }

  // 원클릭 = 선택
  function handleSelect(e, item) {
    e.stopPropagation();
    setSelectedId(item.fridgeItemId);
  }

  // 빈 공간 클릭 = 선택·편집 해제
  function handleBoardClick() {
    setSelectedId(null);
  }

  function enterEdit(e, id) {
    e.stopPropagation();
    setSelectedId(id);
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
      await rotateFridgeItem(item.fridgeItemId, item.rotation || 0);
      setItems((prev) =>
        prev.map((it) => (it.fridgeItemId === id ? { ...it, zone } : it))
      );
      setSelectedId(null);
    } catch (err) {
      setError(err.message);
    }
  }

  function handleRemove(e, id) {
    e.stopPropagation();
    const item = items.find((it) => it.fridgeItemId === id);
    if (item) setRemoveTarget(item);
  }

  async function confirmRemove(alsoDeleteList) {
    if (!removeTarget) return;
    const { fridgeItemId, userIngredientId } = removeTarget;
    try {
      await removeFridgeItem(fridgeItemId);
      if (alsoDeleteList) {
        await deleteIngredient(userIngredientId);
      }
      setItems((prev) => prev.filter((it) => it.fridgeItemId !== fridgeItemId));
      setSelectedId(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setRemoveTarget(null);
    }
  }

  async function handleChangeImage(imageUrl, imageType) {
    try {
      await changeFridgeItemImage(imageEditId, imageUrl, imageType);
      setItems((prev) =>
        prev.map((it) =>
          it.fridgeItemId === imageEditId ? { ...it, imageUrl, imageType } : it
        )
      );
    } catch (err) {
      setError(err.message);
    }
  }

  function startResize(e, item) {
    e.stopPropagation();
    e.preventDefault();
    const el = e.currentTarget.closest(".fridge-item");
    const rect = el.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    const startDist = Math.hypot(e.clientX - centerX, e.clientY - centerY);
    interaction.current = {
      type: "resize", id: item.fridgeItemId,
      centerX, centerY, startDist, startScale: item.scale || 1,
    };
  }

  function startRotate(e, item) {
    e.stopPropagation();
    e.preventDefault();
    const el = e.currentTarget.closest(".fridge-item");
    const rect = el.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    const startAngle = Math.atan2(e.clientY - centerY, e.clientX - centerX);
    interaction.current = {
      type: "rotate", id: item.fridgeItemId,
      centerX, centerY, startAngle, startRotation: item.rotation || 0,
    };
  }



  return (
    <div className="fridge-decorate">
      <h2>내 냉장고</h2>
      {error && <p className="fridge-error">{error}</p>}

      <button className="fridge-add-button" onClick={() => setShowAdd(true)}>
        + 재료 추가
      </button>

      <button className="fridge-label-toggle" onClick={() => setShowLabels((v) => !v)}>
        {showLabels ? "🏷️ ON" : "🏷️ OFF"}
      </button>

      {showAdd && (
        <FridgeAddModal
          onClose={() => setShowAdd(false)}
          onAdded={() => fetchFridgeItems().then(setItems).catch((e) => setError(e.message))}
        />
      )}

      {imageEditId && (
        <FridgeAddModal
          imageOnly
          onClose={() => setImageEditId(null)}
          onPickImage={handleChangeImage}
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
          const scale = item.scale || 1;
          return (
            <div
              key={item.fridgeItemId}
              className={`fridge-item ${selected ? "editing" : ""}`}
              style={{
                left: `${item.posX * 100}%`,
                top: `${item.posY * 100}%`,
                transform: `translate(-50%, -50%) scale(${scale})`,
                cursor: selected ? "grab" : "pointer",
              }}
              onPointerDown={(e) => onPointerDown(e, item)}
              onClick={(e) => handleSelect(e, item)}
            >
              {item.imageType === "SYSTEM" ? (
                <span
                  className="fridge-item-emoji"
                  style={{ transform: `rotate(${item.rotation || 0}deg)`, display: "inline-block" }}
                >{item.imageUrl}</span>
              ) : (
                item.imageUrl && (
                  <img
                    className={item.imageType === "DRAWING" ? "fridge-item-drawing" : ""}
                    src={toImageSrc(item.imageUrl)}
                    alt={item.ingredientName}
                    style={{ transform: `rotate(${item.rotation || 0}deg)` }}
                  />
                ))}

              {showLabels && (
                <span
                  className="fridge-item-label"
                  style={{ transform: `translateX(-50%) scale(${1 / scale})`, transformOrigin: "top center" }}
                >
                  {item.ingredientName}
                </span>
              )}

              {urgent && (
                <span
                  className="fridge-item-dday"
                  style={{ transform: `scale(${1 / scale})`, transformOrigin: "top right" }}
                >
                  {d < 0 ? "만료" : `D-${d}`}
                </span>
              )}

              {selected && (
                <>
                  {/* 크기 조절 핸들 (우하단 모서리) */}
                  <div
                    className="fridge-handle fridge-handle-resize"
                    style={{ transform: `scale(${1 / scale})` }}
                    onPointerDown={(e) => startResize(e, item)}
                  />
                  {/* 회전 핸들 (상단) */}
                  <div
                    className="fridge-handle fridge-handle-rotate"
                    style={{ transform: `scale(${1 / scale})` }}
                    onPointerDown={(e) => startRotate(e, item)}
                  />
                  {/* 하단 버튼: 편집(이미지) + 적용 */}
                  <div
                    className="fridge-item-controls"
                    style={{ transform: `translateX(-50%) scale(${1 / scale})` }}
                    onPointerDown={(e) => e.stopPropagation()}
                  >
                    <button onClick={(e) => { e.stopPropagation(); setImageEditId(item.fridgeItemId); }}>🔧</button>
                    <button className="fridge-item-apply" onClick={(e) => applyEdit(e, item.fridgeItemId)}>적용</button>
                  </div>
                  <button
                    className="fridge-item-remove"
                    style={{ transform: `scale(${1 / scale})` }}
                    onClick={(e) => handleRemove(e, item.fridgeItemId)}
                  >
                    ×
                  </button>
                </>
              )}

            </div>
          );
        })}
      </div>
      {removeTarget && (
        <div className="fdc-remove-overlay" onClick={() => setRemoveTarget(null)}>
          <div className="fdc-remove-modal" onClick={(e) => e.stopPropagation()}>
            <p className="fdc-remove-title">"{removeTarget.ingredientName}"</p>
            <p className="fdc-remove-desc">어떻게 뺄까요?</p>
            <button className="fdc-remove-btn danger" onClick={() => confirmRemove(true)}>
              목록에서도 삭제
            </button>
            <button className="fdc-remove-btn" onClick={() => confirmRemove(false)}>
              냉장고에서만 빼기
            </button>
            <button className="fdc-remove-btn cancel" onClick={() => setRemoveTarget(null)}>
              취소
            </button>
          </div>
        </div>
      )}
    </div>
  );
}