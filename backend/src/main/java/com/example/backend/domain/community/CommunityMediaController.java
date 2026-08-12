package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityMediaUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/community/media")
@RequiredArgsConstructor
public class CommunityMediaController {

    private final CommunityMediaService communityMediaService;

    // 게시글 섹션에 첨부할 이미지/동영상을 미리 업로드하고 URL을 받는다.
    // 예: POST /api/v1/community/media (multipart/form-data, key: file)
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityMediaUploadResponse upload(@RequestParam("file") MultipartFile file) {
        return communityMediaService.upload(file);
    }
}
