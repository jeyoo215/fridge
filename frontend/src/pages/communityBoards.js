// 커뮤니티 게시판 종류 공통 설정. CommunitySidebar/CommunityList/CommunityPostForm/CommunityPostDetail이
// 전부 이 배열을 기준으로 게시판 이름/경로/챌린지 자격 조건을 맞춘다 (게시판을 추가/변경할 때 여기만 고치면 됨).
export const BOARD_CONFIGS = [
  {
    boardType: "RECIPE",
    label: "레시피 게시판",
    listPath: "/community",
    newPath: "/community/new",
    challengeType: null,
  },
  {
    boardType: "CHALLENGE_FRIDGE_CLEAN",
    label: "냉장고 클린 챌린지 게시판",
    listPath: "/community/challenge/fridge-clean",
    newPath: "/community/challenge/fridge-clean/new",
    challengeType: "FRIDGE_CLEAN",
  },
  {
    boardType: "CHALLENGE_TARGET_INGREDIENT",
    label: "재료 소진 챌린지 게시판",
    listPath: "/community/challenge/target-ingredient",
    newPath: "/community/challenge/target-ingredient/new",
    challengeType: "TARGET_INGREDIENT",
  },
  {
    boardType: "FREE_TALK",
    label: "전체 잡담 게시판",
    listPath: "/community/free-talk",
    newPath: "/community/free-talk/new",
    challengeType: null,
  },
];

export const FREE_TALK_PREFIXES = ["다이어터", "20대", "30대", "40대", "50대"];

export function getBoardConfig(boardType) {
  return BOARD_CONFIGS.find((board) => board.boardType === boardType) || BOARD_CONFIGS[0];
}
