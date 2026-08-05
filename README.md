# 🧊 냉장고 파먹기 (Fridge to Table)

## 🚀 프로젝트 시작 가이드 (팀원 공통)

### 1. Repository Clone
```bash
git clone <GitHub 저장소 주소>
cd fridge-to-table
```

### 2. Frontend 실행
```bash
cd frontend
npm install
npm run dev
```

### 3. Backend 실행
BackendApplication.java 파일 실행

---

## 📤 이제 GitHub에 첫 커밋 & 푸시하기!\

```bash
# 1. 변경된 모든 파일 담기
git add .

# 2. 첫 번째 커밋 메시지 작성
git commit -m "chore: 프로젝트 초기 모노레포 세팅 및 공통 설정 완료"

# 3. 기본 브랜치를 main으로 설정
git branch -M main

# 4. GitHub 저장소 연결 (GitHub에서 만든 본인 레포 주소 입력)
git remote add origin https://github.com/사용자이름/저장소이름.git

# 5. 푸시하기
git push -u origin main
```


---


### 브랜치 이동 및 작성

VS Code에서 브랜치 만들고 작업하는 순서

1단계: 작업 시작 전! 최신 코드 받아오기 (develop 기준)
```bash
git checkout develop
git pull origin develop
```
핵심 법칙: "작업 시작 전에 pull!" 묵은 코드 위에서 작업하면 충돌이 생깁니다.

2단계: 내 전용 작업실(브랜치) 만들고 이동하기
```bash
# feature/기능이름 으로 새 브랜치를 만들면서 이동
git checkout -b feature/login
(VS Code 맨 왼쪽 아래 구석을 보면 main이던 글자가 feature/login으로 바뀐 것을 확인할 수 있습니다!)
```

3단계: 내 브랜치에서 신나게 코딩하고 커밋하기
```bash
git add .
git commit -m "feat: 로그인 API 구현"
```
4단계: 내 브랜치만 깃허브에 올리기 (퇴근/하교 전!)
```bash
git push origin feature/login
```
핵심 법칙: "집에 가기 전에 push!" 이렇게 올리면 깃허브 상에서 내가 만든 feature/login 코드를 develop에 합쳐달라고 요청(PR: Pull Request)을 보낼 수 있게 됩니다.


### 커밋 메세지

| 종류 | 언제 쓰나 | 예시 |
| --- | --- | --- |
| `feat` | 새 기능을 만들었을 때 | `feat: 로그인 API 구현` |
| `fix` | 버그를 고쳤을 때 | `fix: 비밀번호 틀려도 로그인되는 오류 수정` |
| `refactor` | 기능은 그대로, 코드만 정리 | `refactor: 게시글 조회 로직 분리` |
| `docs` | 문서만 수정 | `docs: README에 실행 방법 추가` |
| `test` | 테스트 코드 | `test: 회원가입 테스트 추가` |
| `chore` | 설정 파일, 라이브러리 추가 등 | `chore: MySQL 드라이버 추가` |