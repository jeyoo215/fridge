package com.example.backend.domain.community;

import com.example.backend.domain.community.dto.CommunityMediaUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityMediaService {

    private static final long MAX_IMAGE_SIZE = 50L * 1024 * 1024;  // 50MB
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024; // 100MB

    // S3Config가 aws.s3.bucket 설정이 있을 때만 빈을 만들어주므로, 없으면 Optional.empty()로 주입된다
    // (Spring이 Optional<T> 의존성은 빈이 없어도 에러 없이 비워서 넣어줌).
    private final Optional<S3Client> s3Client;

    @Value("${community.media.upload-dir:uploads/community}")
    private String uploadDir;

    @Value("${aws.s3.bucket:}")
    private String bucket;

    @Value("${aws.s3.region:}")
    private String region;

    public CommunityMediaUploadResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        CommunityPostStep.MediaType mediaType = detectMediaType(file.getContentType());
        validateSize(file.getSize(), mediaType);

        String extension = extractExtension(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + extension;

        String url = s3Client.isPresent()
                ? uploadToS3(file, storedFileName)
                : uploadToLocalDisk(file, storedFileName);

        return new CommunityMediaUploadResponse(url, mediaType);
    }

    // DB(RDS)는 공용인데 이미지 파일이 각자 로컬 디스크에만 있으면 다른 컴퓨터에선 깨져 보이므로,
    // S3가 설정돼 있으면 공용 저장소인 S3로 올려서 절대 URL을 반환한다.
    private String uploadToS3(MultipartFile file, String storedFileName) {
        String key = "community/" + storedFileName;
        try {
            s3Client.get().putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new UncheckedIOException("파일 업로드에 실패했습니다.", e);
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    // S3를 아직 설정 안 한 팀원을 위한 예전 방식 폴백. 이 경우 업로드한 본인 컴퓨터에서만
    // 이미지가 보이는 한계(원래 문제)는 그대로 남는다.
    private String uploadToLocalDisk(MultipartFile file, String storedFileName) {
        try {
            Path targetDir = Path.of(uploadDir);
            Files.createDirectories(targetDir);
            file.transferTo(targetDir.resolve(storedFileName));
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장에 실패했습니다.", e);
        }
        return "/media/community/" + storedFileName;
    }

    private CommunityPostStep.MediaType detectMediaType(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("파일 형식을 확인할 수 없습니다.");
        }
        if (contentType.startsWith("image/")) {
            return CommunityPostStep.MediaType.IMAGE;
        }
        if (contentType.startsWith("video/")) {
            return CommunityPostStep.MediaType.VIDEO;
        }
        throw new IllegalArgumentException("이미지 또는 동영상 파일만 업로드할 수 있습니다.");
    }

    private void validateSize(long size, CommunityPostStep.MediaType mediaType) {
        long limit = mediaType == CommunityPostStep.MediaType.IMAGE ? MAX_IMAGE_SIZE : MAX_VIDEO_SIZE;
        if (size > limit) {
            String limitLabel = mediaType == CommunityPostStep.MediaType.IMAGE ? "50MB" : "100MB";
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
