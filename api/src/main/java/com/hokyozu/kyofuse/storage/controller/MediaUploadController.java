package com.hokyozu.kyofuse.storage.controller;

import com.hokyozu.kyofuse.storage.dto.response.MediaUploadResponse;
import com.hokyozu.kyofuse.storage.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaUploadController {

    private final ImageProcessingService imageProcessingService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaUploadResponse upload(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return imageProcessingService.processAndUpload(userId, file);
    }
}
