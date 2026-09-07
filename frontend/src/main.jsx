import './styles/theme.css'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

// StrictMode를 껐습니다: 개발 모드에서 컴포넌트를 일부러 두 번 마운트하는 StrictMode 특성 때문에
// Quill 에디터가 "바깥 클릭 감지" 리스너를 중복으로 붙여서, 색상/크기 팔레트가 클릭을 떼자마자
// 바로 닫혀버리는 버그가 있었음. StrictMode는 프로덕션 빌드에는 영향이 없는 개발 전용 진단 도구라
// 꺼도 배포에는 지장 없음.
createRoot(document.getElementById('root')).render(
  <App />,
)
