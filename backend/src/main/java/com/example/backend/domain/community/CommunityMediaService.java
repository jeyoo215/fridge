package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityMediaUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityMediaService {

    private static final long MAX_IMAGE_SIZE = 50L * 1024 * 1024;  // 50MB
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024; // 100MB

    @Value("${community.media.upload-dir:uploads/community}")
    private String uploadDir;

    public CommunityMediaUploadResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        CommunityPostSection.MediaType mediaType = detectMediaType(file.getContentType());
        validateSize(file.getSize(), mediaType);

        String extension = extractExtension(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + extension;

        try {
            Path targetDir = Path.of(uploadDir);
            Files.createDirectories(targetDir);
            file.transferTo(targetDir.resolve(storedFileName));
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장에 실패했습니다.", e);
        }

        // 호스트는 프론트가 이미 API 호출에 쓰는 것과 동일하게(LAN IP 등) 붙여야 해서, 절대 URL이 아닌 상대 경로만 내려준다.
        String url = "/media/community/" + storedFileName;
        return new CommunityMediaUploadResponse(url, mediaType);
    }

    private CommunityPostSection.MediaType detectMediaType(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("파일 형식을 확인할 수 없습니다.");
        }
        if (contentType.startsWith("image/")) {
            return CommunityPostSection.MediaType.IMAGE;
        }
        if (contentType.startsWith("video/")) {
            return CommunityPostSection.MediaType.VIDEO;
        }
        throw new IllegalArgumentException("이미지 또는 동영상 파일만 업로드할 수 있습니다.");
    }

    private void validateSize(long size, CommunityPostSection.MediaType mediaType) {
        long limit = mediaType == CommunityPostSection.MediaType.IMAGE ? MAX_IMAGE_SIZE : MAX_VIDEO_SIZE;
        if (size > limit) {
            String limitLabel = mediaType == CommunityPostSection.MediaType.IMAGE ? "50MB" : "100MB";
            throw new IllegalArgumentException(mediaType + " 파일은 " + limitLabel + "를 넘을 수 없습니다.");
        }
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf('.'));
    }
}
