import { useState } from "react";
import { searchIngredients } from "../api/ingredientApi";
import { createFridgeItem, uploadImage } from "../api/fridgeApi";
import "./FridgeAddModal.css";

const SYSTEM_EMOJIS = [
    "🥬", "🥕", "🧅", "🧄", "🥩", "🍗", "🐟", "🥚",
    "🥛", "🧀", "🍎", "🍌", "🍓", "🍅", "🌶️", "🥔",
    "🍞", "🧈", "🥫", "🧊",
];

export default function FridgeAddModal({ onClose, onAdded }) {
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

    async function handleSave() {
        setError("");
        if (!selectedIngredient) {
            setError("재료를 선택해주세요.");
            return;
        }
        if (!expirationDate) {
            setError("유통기한을 입력해주세요.");
            return;
        }
        if (tab === "system" && !emoji) {
            setError("이미지를 선택해주세요.");
            return;
        }
        if (tab === "photo" && !photoUrl) {
            setError("사진을 업로드해주세요.");
            return;
        }

        const payload = {
            ingredientId: selectedIngredient.ingredientId,
            quantity: 1,
            unit: "개",
            purchaseDate: null,
            expirationDate,
            imageUrl: tab === "system" ? emoji : tab === "photo" ? photoUrl : null,
            imageType: tab === "system" ? "SYSTEM" : tab === "photo" ? "PHOTO" : "DRAWING",
            posX: 0.5,
            posY: 0.5,
            zone: "FRIDGE",
        };

        try {
            setSaving(true);
            await createFridgeItem(payload);
            onAdded();
            onClose();
        } catch (e) {
            setError(e.message);
        } finally {
            setSaving(false);
        }
    }

    return (
        <div className="fam-overlay" onClick={onClose}>
            <div className="fam-modal" onClick={(e) => e.stopPropagation()}>
                <div className="fam-header">
                    <h3>재료 추가</h3>
                    <button className="fam-close" onClick={onClose}>×</button>
                </div>

                <div className="fam-field">
                    <label>재료명</label>
                    <input
                        value={keyword}
                        onChange={(e) => handleSearch(e.target.value)}
                        placeholder="재료 검색 (예: 양파)"
                    />
                    {candidates.length > 0 && (
                        <ul className="fam-candidates">
                            {candidates.map((ing) => (
                                <li key={ing.ingredientId} onClick={() => pickIngredient(ing)}>
                                    {ing.ingredientName}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                <div className="fam-field">
                    <label>유통기한</label>
                    <input
                        type="date"
                        value={expirationDate}
                        onChange={(e) => setExpirationDate(e.target.value)}
                    />
                </div>

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
                                    onClick={() => setEmoji(em)}
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

                    {tab === "drawing" && <p className="fam-todo">그림 그리기 (준비 중)</p>}

                </div>

                {error && <p className="fam-error">{error}</p>}

                <button className="fam-save" onClick={handleSave} disabled={saving}>
                    {saving ? "저장 중..." : "추가"}
                </button>
            </div>
        </div>
    );
}