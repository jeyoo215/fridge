import { useState, useEffect, useRef } from "react";
import { searchIngredients } from "../api/ingredientApi";
import { createFridgeItem, uploadImage, fetchUnplacedIngredients, placeFridgeItem } from "../api/fridgeApi";

import "./FridgeAddModal.css";

const SYSTEM_EMOJIS = [
    "🥬", "🥕", "🧅", "🧄", "🥩", "🍗", "🐟", "🥚",
    "🥛", "🧀", "🍎", "🍌", "🍓", "🍅", "🌶️", "🥔",
    "🍞", "🧈", "🥫", "🧊",
];

export default function FridgeAddModal({ onClose, onAdded, imageOnly = false, onPickImage }) {
    const [tab, setTab] = useState("system"); // system | photo | drawing
    const [keyword, setKeyword] = useState("");
    const [candidates, setCandidates] = useState([]);
    const [selectedIngredient, setSelectedIngredient] = useState(null);
    const [expirationDate, setExpirationDate] = useState("");
    const [emoji, setEmoji] = useState("");
    const [error, setError] = useState("");
    const [saving, setSaving] = useState(false);
    const [photoUrl, setPhotoUrl] = useState("");
    const [uploading, setUploading] = useState(false);

    // 보유재료 가져오기
    const [showExisting, setShowExisting] = useState(false); // 목록 화면 표시 여부
    const [unplaced, setUnplaced] = useState([]);
    const [selectedUnplaced, setSelectedUnplaced] = useState(null); // 선택한 보유재료

    // 그림 그리기
    const canvasRef = useRef(null);
    const drawing = useRef(false);
    const [penColor, setPenColor] = useState("#333333");
    const [eraser, setEraser] = useState(false);
    const [penSize, setPenSize] = useState(4);

    useEffect(() => {
        if (showExisting) {
            fetchUnplacedIngredients()
                .then((data) => setUnplaced(Array.isArray(data) ? data : []))
                .catch((e) => setError(e.message));
        }
    }, [showExisting]);

    async function handleSearch(value) {
        setKeyword(value);
        setSelectedIngredient(null);
        if (value.trim().length < 1) {
            setCandidates([]);
            return;
        }
        try {
            const result = await searchIngredients(value);
            setCandidates(result);
        } catch {
            setCandidates([]);
        }
    }

    function pickIngredient(ing) {
        setSelectedIngredient(ing);
        setKeyword(ing.ingredientName);
        setCandidates([]);
    }

    async function handlePhotoSelect(e) {
        const file = e.target.files[0];
        if (!file) return;
        setError("");
        try {
            setUploading(true);
            const url = await uploadImage(file);
            setPhotoUrl(url);
        } catch (err) {
            setError(err.message);
        } finally {
            setUploading(false);
        }
    }

    // 보유재료 목록에서 하나 선택 -> 등록 화면으로 복귀
    function pickUnplaced(ui) {
        setSelectedUnplaced(ui);
        setShowExisting(false);
        setTab("system");
        setEmoji("");
        setPhotoUrl("");
    }

    async function resolveImage() {
        if (tab === "drawing") {
            const url = await uploadDrawing();
            return { imageUrl: url, imageType: "DRAWING" };
        }
        return {
            imageUrl: tab === "system" ? emoji : photoUrl,
            imageType: tab === "system" ? "SYSTEM" : "PHOTO",
        };
    }

    function validateImage() {
        if (tab === "system" && !emoji) return "이미지를 선택해주세요.";
        if (tab === "photo" && !photoUrl) return "사진을 업로드해주세요.";
        return null;
    }

    // 신규 재료 등록 + 배치
    async function handleSave() {
        setError("");
        if (!selectedIngredient) return setError("재료를 선택해주세요.");
        if (!expirationDate) return setError("유통기한을 입력해주세요.");
        const imgErr = validateImage();
        if (imgErr) return setError(imgErr);

        try {
            setSaving(true);
            const img = await resolveImage();
            const payload = {
                ingredientId: selectedIngredient.ingredientId,
                quantity: 1,
                unit: "개",
                purchaseDate: null,
                expirationDate,
                imageUrl: img.imageUrl,
                imageType: img.imageType,
                posX: 0.5,
                posY: 0.5,
                zone: "FRIDGE",
            };
            await createFridgeItem(payload);
            onAdded();
            onClose();
        } catch (e) {
            setError(e.message);
        } finally {
            setSaving(false);
        }
    }

    async function handleImageOnly() {
        setError("");
        const imgErr = validateImage();
        if (imgErr) return setError(imgErr);
        try {
            setSaving(true);
            const img = await resolveImage();
            onPickImage(img.imageUrl, img.imageType);
            onClose();
        } catch (e) {
            setError(e.message);
        } finally {
            setSaving(false);
        }
    }

    // 보유재료 배치
    async function handlePlaceExisting() {
        setError("");
        if (!selectedUnplaced) return setError("배치할 재료를 선택해주세요.");
        const imgErr = validateImage();
        if (imgErr) return setError(imgErr);

        try {
            setSaving(true);
            const img = await resolveImage();
            await placeFridgeItem({
                userIngredientId: selectedUnplaced.userIngredientId,
                imageUrl: img.imageUrl,
                imageType: img.imageType,
                posX: 0.5,
                posY: 0.5,
                zone: "FRIDGE",
            });
            onAdded();
            onClose();
        } catch (e) {
            setError(e.message);
        } finally {
            setSaving(false);
        }
    }

    // 그리기 핸들러 
    function draw(e) {
        if (!drawing.current) return;
        const canvas = canvasRef.current;
        const rect = canvas.getBoundingClientRect();
        const x = (e.clientX - rect.left) * (canvas.width / rect.width);
        const y = (e.clientY - rect.top) * (canvas.height / rect.height);
        const ctx = canvas.getContext("2d");
        ctx.lineCap = "round";
        ctx.lineJoin = "round";
        ctx.globalCompositeOperation = eraser ? "destination-out" : "source-over";
        ctx.lineWidth = eraser ? penSize * 3 : penSize;
        ctx.strokeStyle = eraser ? "rgba(0,0,0,1)" : penColor;
        ctx.lineTo(x, y);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(x, y);
    }

    function startDraw(e) {
        drawing.current = true;
        canvasRef.current.getContext("2d").beginPath();
        draw(e);
    }


    function endDraw() {
        drawing.current = false;
        canvasRef.current?.getContext("2d").beginPath();
    }

    function clearCanvas() {
        const canvas = canvasRef.current;
        canvas.getContext("2d").clearRect(0, 0, canvas.width, canvas.height);
    }
    async function uploadDrawing() {
        const canvas = canvasRef.current;
        const ctx = canvas.getContext("2d");
        const { width, height } = canvas;
        const data = ctx.getImageData(0, 0, width, height).data;

        // 그려진 픽셀 경계 찾기
        let minX = width, minY = height, maxX = 0, maxY = 0, found = false;
        for (let y = 0; y < height; y++) {
            for (let x = 0; x < width; x++) {
                const alpha = data[(y * width + x) * 4 + 3];
                if (alpha > 0) {
                    found = true;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (!found) throw new Error("그림을 그려주세요.");

        const pad = 8;
        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(width, maxX + pad);
        maxY = Math.min(height, maxY + pad);
        const cw = maxX - minX;
        const ch = maxY - minY;

        // 크롭된 영역만 새 캔버스에 복사
        const cropped = document.createElement("canvas");
        cropped.width = cw;
        cropped.height = ch;
        cropped.getContext("2d").drawImage(canvas, minX, minY, cw, ch, 0, 0, cw, ch);

        return new Promise((resolve, reject) => {
            cropped.toBlob(async (blob) => {
                if (!blob) return reject(new Error("그림 변환 실패"));
                const file = new File([blob], "drawing.png", { type: "image/png" });
                try {
                    resolve(await uploadImage(file));
                } catch (err) {
                    reject(err);
                }
            }, "image/png");
        });
    }

    // ---------- 화면 1: 보유재료 목록 ----------
    if (showExisting) {
        return (
            <div className="fam-overlay" onClick={onClose}>
                <div className="fam-modal" onClick={(e) => e.stopPropagation()}>
                    <div className="fam-header">
                        <h3>보유재료에서 가져오기</h3>
                        <button className="fam-close" onClick={onClose}>×</button>
                    </div>

                    {error && <p className="fam-error">{error}</p>}

                    {unplaced.length === 0 ? (
                        <p className="fam-todo">배치할 보유재료가 없어요</p>
                    ) : (
                        <ul className="fam-existing-list">
                            {unplaced.map((ui) => (
                                <li key={ui.userIngredientId} onClick={() => pickUnplaced(ui)}>
                                    {ui.ingredientName} (D-{ui.dDay})
                                </li>
                            ))}
                        </ul>
                    )}

                    <button className="fam-save" onClick={() => setShowExisting(false)}>
                        ← 취소
                    </button>
                </div>
            </div>
        );
    }

    // ---------- 화면 2: 등록/배치 (신규·보유 공용) ----------
    const nameValue = selectedUnplaced ? selectedUnplaced.ingredientName : keyword;
    const dateValue = selectedUnplaced ? selectedUnplaced.expirationDate : expirationDate;
    const locked = !!selectedUnplaced;

    return (
        <div className="fam-overlay" onClick={onClose}>
            <div className="fam-modal" onClick={(e) => e.stopPropagation()}>
                <div className="fam-header">
                    <h3>{imageOnly ? "이미지 변경" : (locked ? "보유재료 배치" : "재료 추가")}</h3>
                    <button className="fam-close" onClick={onClose}>×</button>
                </div>

                {!imageOnly && (
                    <div className="fam-field">
                    <label>재료명</label>
                    <input
                        value={nameValue}
                        onChange={(e) => handleSearch(e.target.value)}
                        readOnly={locked}
                        placeholder="재료명 (예: 양파)"
                    />
                    {!locked && candidates.length > 0 && (
                        <ul className="fam-candidates">
                            {candidates.map((ing) => (
                                <li key={ing.ingredientId} onClick={() => pickIngredient(ing)}>
                                    {ing.ingredientName}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
                )}

                {!imageOnly && (
                    <div className="fam-field">
                        <label>유통기한</label>
                        <input
                            type="date"
                            value={dateValue || ""}
                            onChange={(e) => setExpirationDate(e.target.value)}
                            readOnly={locked}
                        />
                    </div>
                )}


                {!imageOnly && !locked && (
                <div className="fam-existing-toggle">
                    <button
                        className="fam-existing-btn"
                        onClick={() => { setShowExisting(true); setError(""); }}
                    >
                        📦 보유재료에서 가져오기
                    </button>
                </div>
                )}
            

                 {!imageOnly && locked && (
                    <div className="fam-existing-toggle">
                        <button
                            className="fam-existing-btn"
                            onClick={() => { setSelectedUnplaced(null); setShowExisting(true); }}
                        >
                            ← 다른 보유재료 선택
                        </button>
                    </div>
                )}

                <div className="fam-tabs">
                    <button className={tab === "system" ? "active" : ""} onClick={() => setTab("system")}>시스템</button>
                    <button className={tab === "photo" ? "active" : ""} onClick={() => setTab("photo")}>사진</button>
                    <button className={tab === "drawing" ? "active" : ""} onClick={() => setTab("drawing")}>그림</button>
                </div>

                <div className="fam-tab-body">
                    {tab === "system" && (
                        <div className="fam-emoji-grid">
                            {SYSTEM_EMOJIS.map((em) => (
                                <button
                                    key={em}
                                    className={`fam-emoji ${emoji === em ? "selected" : ""}`}
                                    onClick={() => { setEmoji(em); setPhotoUrl(""); }}
                                >
                                    {em}
                                </button>
                            ))}
                        </div>
                    )}

                    {tab === "photo" && (
                        <div className="fam-photo">
                            <input type="file" accept="image/*" onChange={handlePhotoSelect} />
                            {uploading && <p className="fam-todo">업로드 중...</p>}
                            {photoUrl && (
                                <img className="fam-photo-preview" src={`http://${window.location.hostname}:8080${photoUrl}`} alt="미리보기" />
                            )}
                        </div>
                    )}

                    {tab === "drawing" && (
                        <div className="fam-drawing">
                            <div>
                                <button className="fam-clear" onClick={clearCanvas}>전체 지우기</button>
                            </div>
                            <canvas
                                ref={canvasRef}
                                width={320}
                                height={240}
                                className="fam-canvas"
                                onPointerDown={startDraw}
                                onPointerMove={draw}
                                onPointerUp={endDraw}
                                onPointerLeave={endDraw}
                            />
                            <div className="fam-draw-tools">

                                <input
                                    type="color"
                                    value={penColor}
                                    onChange={(e) => { setPenColor(e.target.value); setEraser(false); }}
                                    className="fam-color-picker"
                                />
                                <button
                                    className={`fam-tool ${!eraser ? "active" : ""}`}
                                    onClick={() => setEraser(false)}
                                >
                                    ✏️ 펜
                                </button>
                                <button
                                    className={`fam-tool ${eraser ? "active" : ""}`}
                                    onClick={() => setEraser(true)}
                                >
                                    ⬜ 지우개
                                </button>
                                <input
                                    type="range"
                                    min="1"
                                    max="20"
                                    value={penSize}
                                    onChange={(e) => setPenSize(Number(e.target.value))}
                                    className="fam-size-slider"
                                />

                            </div>


                        </div>
                    )}
                </div>

                {error && <p className="fam-error">{error}</p>}

                <button
                    className="fam-save"
                    onClick={imageOnly ? handleImageOnly : (locked ? handlePlaceExisting : handleSave)}
                    disabled={saving}
                >
                    {saving ? "저장 중..." : (imageOnly ? "변경" : "추가")}
                </button>
            </div>
        </div>
    );
}
