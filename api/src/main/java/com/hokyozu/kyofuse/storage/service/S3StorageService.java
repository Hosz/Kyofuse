package com.hokyozu.kyofuse.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.CORSConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${storage.s3.bucket:kyofuse-media}")
    private String bucketName;

    @Value("${storage.s3.public-url:http://localhost:9000}")
    private String publicBaseUrl;

    private volatile boolean bucketConfigured = false;

    public String uploadFile(String key, byte[] bytes, String contentType) {
        ensureBucketExists();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));
        return getPublicUrl(key);
    }

    public void deleteFile(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    public String getPublicUrl(String key) {
        String baseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        String cleanKey = key.startsWith("/") ? key.substring(1) : key;
        if (baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1") || baseUrl.contains("minio")) {
            return String.format("%s/%s/%s", baseUrl, bucketName, cleanKey);
        }
        return String.format("%s/%s", baseUrl, cleanKey);
    }

    private synchronized void ensureBucketExists() {
        if (bucketConfigured) {
            return;
        }

        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            log.info("Bucket {} does not exist. Creating it now...", bucketName);
            try {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            } catch (Exception ex) {
                log.warn("Failed creating bucket {}: {}", bucketName, ex.getMessage());
            }
        } catch (Exception e) {
            try {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            } catch (Exception ignored) {
            }
        }

        configureBucketPolicy();
        configureBucketCors();
        bucketConfigured = true;
    }

    private void configureBucketPolicy() {
        try {
            String policy = String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Sid": "PublicReadGetObject",
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                        }
                    ]
                }
                """, bucketName);

            s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .policy(policy)
                    .build());
            log.info("Public read policy successfully applied to bucket {}", bucketName);
        } catch (Exception e) {
            log.warn("Could not set public policy on bucket {}: {}", bucketName, e.getMessage());
        }
    }

    private void configureBucketCors() {
        try {
            CORSRule corsRule = CORSRule.builder()
                    .allowedOrigins("*")
                    .allowedMethods("GET", "HEAD")
                    .allowedHeaders("*")
                    .maxAgeSeconds(3000)
                    .build();

            CORSConfiguration configuration = CORSConfiguration.builder()
                    .corsRules(List.of(corsRule))
                    .build();

            s3Client.putBucketCors(PutBucketCorsRequest.builder()
                    .bucket(bucketName)
                    .corsConfiguration(configuration)
                    .build());
            log.info("CORS policy successfully applied to bucket {}", bucketName);
        } catch (Exception e) {
            log.warn("Could not set CORS on bucket {}: {}", bucketName, e.getMessage());
        }
    }
}