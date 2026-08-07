package com.example.backend.domain.social;

import com.example.backend.domain.social.dto.PopularRecipeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PopularRecipeController {

    private final PopularRecipeService popularRecipeService;

    // 예: GET /api/v1/recipes/popular?sortBy=likes   (또는 sortBy=reviews)
    @GetMapping("/api/v1/recipes/popular")
    public List<PopularRecipeResponse> getPopularRecipes(
            @RequestParam(name = "sortBy", defaultValue = "likes") String sortBy) {
        return popularRecipeService.getPopularRecipes(sortBy);
    }
}
