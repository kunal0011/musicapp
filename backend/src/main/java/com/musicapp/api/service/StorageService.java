package com.musicapp.api.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioClient minioClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    private static final int PRESIGNED_EXPIRY_MINUTES = 60;
    private static final String PRESIGNED_CACHE_PREFIX = "presigned:";

    public String uploadFile(MultipartFile file, String folder) throws Exception {
        String extension = getExtension(file.getOriginalFilename());
        String objectName = folder + "/" + UUID.randomUUID() + extension;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

        return minioUrl + "/" + bucketName + "/" + objectName;
    }

    public String generatePresignedUrl(String fullUrl, int expiryMinutes) throws Exception {
        String objectName = fullUrl.replace(minioUrl + "/" + bucketName + "/", "");
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .method(Method.GET)
                        .expiry(expiryMinutes, TimeUnit.MINUTES)
                        .build());
    }

    /**
     * Returns a cached pre-signed URL from Redis, or generates + caches a new one.
     * TTL is set to (expiry - 1 minute) to avoid serving expired URLs.
     */
    public String getCachedPresignedUrl(String fullUrl) {
        if (fullUrl == null || fullUrl.isEmpty())
            return fullUrl;

        String cacheKey = PRESIGNED_CACHE_PREFIX + fullUrl;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            String presigned = generatePresignedUrl(fullUrl, PRESIGNED_EXPIRY_MINUTES);
            // Cache with TTL = expiry - 1 min buffer
            stringRedisTemplate.opsForValue().set(
                    cacheKey, presigned,
                    Duration.ofMinutes(PRESIGNED_EXPIRY_MINUTES - 1));
            return presigned;
        } catch (Exception e) {
            // Fall back to the raw URL if presigning fails
            return fullUrl;
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return "";
        return "." + filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
