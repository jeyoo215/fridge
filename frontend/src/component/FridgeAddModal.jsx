import "./FridgeAddModal.css";

export default function FridgeAddModal({ onClose, onAdded }) {
  return (
    <div className="fam-overlay" onClick={onClose}>
      <div className="fam-modal" onClick={(e) => e.stopPropagation()}>
        <div className="fam-header">
          <h3>재료 추가</h3>
          <button type="button" className="fam-close" onClick={onClose}>
            ×
          </button>
        </div>

        <p className="fam-todo">준비 중이에요 🚧</p>
      </div>
    </div>
  );
}