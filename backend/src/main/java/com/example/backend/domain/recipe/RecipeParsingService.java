package com.example.backend.domain.recipe;

import com.example.backend.domain.recipe.dto.api.ParsedIngredient;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeParsingService {

    private final RecipeRepository recipeRepository;
    private final RecipeParsingWorker worker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.url}")
    private String apiUrl;

    // @Transactional 없음 — 건별로 워커가 개별 커밋
    public int parseRecipes(int limit) {
        List<Recipe> targets = recipeRepository.findRecipesToParse(
                org.springframework.data.domain.PageRequest.of(0, limit));
        log.info("파싱 대상 {}건", targets.size());

        int success = 0;
        for (Recipe recipe : targets) {
            try {
                List<ParsedIngredient> parsed = callClaude(recipe.getRawIngredients());
                worker.saveParsedIngredients(recipe.getRecipeId(), parsed); // 건별 커밋
                success++;
                log.info("[{}] {} → 재료 {}개", recipe.getRecipeId(), recipe.getRecipeName(), parsed.size());
            } catch (Exception e) {
                log.error("파싱 실패 [{}] {}: {}", recipe.getRecipeId(), recipe.getRecipeName(), e.getMessage());
            }
        }
        log.info("파싱 완료: {}/{}건 성공", success, targets.size());
        return success;
    }

    private List<ParsedIngredient> callClaude(String rawIngredients) throws Exception {
        String prompt = """
                다음은 한국 요리 레시피의 재료 문자열이다. 여기서 순수 재료만 추출해라.
                규칙:
                - name: 재료명을 표준형으로 정규화 (예: "대파(흰부분)" → "대파", "다진 마늘" → "마늘", "계란" → "달걀")
                - isSeasoning: 소금/간장/설탕/참기름/후추/고춧가루/식용유/깨 같은 조미료면 true, 아니면 false
                - quantity: 분량 숫자 (예: "75g" → 75). "약간", "적당량" 등 애매하면 null
                - unit: 단위 (예: "g", "개", "큰술"). 없으면 null
                - 맨 앞의 요리 이름이나 "고명", "양념장" 같은 카테고리 구분자는 재료가 아니므로 제외
                - JSON 배열로만 답하라. 다른 설명 없이.

                재료 문자열:
                %s
                """.formatted(rawIngredients);

        Map<String, Object> body = Map.of(
                "model", "claude-haiku-4-5-20251001",
                "max_tokens", 1024,
                "messages", List.of(Map.of("role", "user", "content", prompt)));

        RestClient restClient = RestClient.create();
        Map<?, ?> response = restClient.post()
                .uri(apiUrl)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        List<?> content = (List<?>) response.get("content");
        Map<?, ?> firstBlock = (Map<?, ?>) content.get(0);
        String text = (String) firstBlock.get("text");
        text = text.replaceAll("```json", "").replaceAll("```", "").trim();

        return objectMapper.readValue(text,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ParsedIngredient.class));
    }
}