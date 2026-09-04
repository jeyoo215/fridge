package com.example.backend.domain.fridge;

import com.example.backend.domain.ingredient.UserIngredient;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 냉장고 화면에 배치된 재료 하나 (위치 + 이미지 + 냉동/냉장 구역)
@Entity
@Table(name = "fridge_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FridgeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_item_id")
    private Long fridgeItemId;

    // 어떤 보유재료를 배치한 건지 (재료 도메인 참조만, 수정 안 함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_ingredient_id", nullable = false)
    private UserIngredient userIngredient;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", columnDefinition = "VARCHAR(10)")
    private ImageType imageType;

    // 냉장고 안 위치 (비율 0.0~1.0 로 저장 — 화면 크기 달라도 일관)
    @Column(name = "pos_x", nullable = false)
    private Double posX;

    @Column(name = "pos_y", nullable = false)
    private Double posY;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone", columnDefinition = "VARCHAR(10)")
    private Zone zone;

    @Column(name = "scale")
    private Double scale;

    @Builder // 둔 위치 저장
    public FridgeItem(UserIngredient userIngredient, String imageUrl, ImageType imageType,
                      Double posX, Double posY, Zone zone) {
        this.userIngredient = userIngredient;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.posX = posX;
        this.posY = posY;
        this.zone = zone;
        this.scale = (scale == null) ? 1.0 : scale;
    }

    // 크기 조정
    public void resize(Double scale) {
        this.scale = scale;
    }

    // 이미지 변경
    public void changeImage(String imageUrl, ImageType imageType) {
        this.imageUrl = imageUrl;
        this.imageType = imageType;
    }

    // 위치/구역 이동
    public void moveTo(Double posX, Double posY, Zone zone) {
        this.posX = posX;
        this.posY = posY;
        this.zone = zone;
    }

    public enum ImageType {
        PHOTO, DRAWING, SYSTEM
    }

    public enum Zone {
        FRIDGE, FROZEN
    }
}