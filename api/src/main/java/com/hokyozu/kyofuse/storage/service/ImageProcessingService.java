package com.hokyozu.kyofuse.storage.service;

import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.storage.dto.response.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Tika tika = new Tika();
    private final S3StorageService storageService;

    public MediaUploadResponse processAndUpload(UUID userId, MultipartFile file) throws IOException {
        validateImage(file);

        byte[] originalBytes = file.getBytes();
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (bufferedImage == null) {
            throw new BadRequestException("Invalid image file format");
        }
        bufferedImage = ensureRgb(bufferedImage);

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        String fileId = UUID.randomUUID().toString();
        String fileKey = String.format("media/%s/%s.jpg", userId, fileId);
        String thumbKey = String.format("media/%s/%s_thumb.jpg", userId, fileId);

        // Process main image (scale down if width > 1920 to save bandwidth)
        ByteArrayOutputStream mainOut = new ByteArrayOutputStream();
        if (width > 1920 || height > 1920) {
            Thumbnails.of(bufferedImage)
                    .size(1920, 1920)
                    .outputFormat("jpg")
                    .outputQuality(0.88)
                    .toOutputStream(mainOut);
        } else {
            Thumbnails.of(bufferedImage)
                    .scale(1.0)
                    .outputFormat("jpg")
                    .outputQuality(0.88)
                    .toOutputStream(mainOut);
        }
        byte[] mainBytes = mainOut.toByteArray();

        // Process thumbnail (max 400x400)
        ByteArrayOutputStream thumbOut = new ByteArrayOutputStream();
        Thumbnails.of(bufferedImage)
                .size(400, 400)
                .outputFormat("jpg")
                .outputQuality(0.80)
                .toOutputStream(thumbOut);
        byte[] thumbBytes = thumbOut.toByteArray();

        String mainUrl = storageService.uploadFile(fileKey, mainBytes, "image/jpeg");
        String thumbUrl = storageService.uploadFile(thumbKey, thumbBytes, "image/jpeg");

        return new MediaUploadResponse(
                fileKey,
                mainUrl,
                thumbUrl,
                "image/jpeg",
                (long) mainBytes.length,
                width,
                height
        );
    }

    public String processAndUploadAvatar(UUID ownerId, String folder, MultipartFile file) throws IOException {
        validateImage(file);

        byte[] originalBytes = file.getBytes();
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (bufferedImage == null) {
            throw new BadRequestException("Invalid image file format");
        }
        bufferedImage = ensureRgb(bufferedImage);

        String fileId = UUID.randomUUID().toString();
        String fileKey = String.format("avatars/%s/%s/%s.jpg", folder, ownerId, fileId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(bufferedImage)
                .size(512, 512)
                .crop(Positions.CENTER)
                .outputFormat("jpg")
                .outputQuality(0.90)
                .toOutputStream(out);

        byte[] avatarBytes = out.toByteArray();
        return storageService.uploadFile(fileKey, avatarBytes, "image/jpeg");
    }

    public String processAndUploadBanner(UUID ownerId, String folder, MultipartFile file) throws IOException {
        validateImage(file);

        byte[] originalBytes = file.getBytes();
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (bufferedImage == null) {
            throw new BadRequestException("Invalid image file format");
        }
        bufferedImage = ensureRgb(bufferedImage);

        String fileId = UUID.randomUUID().toString();
        String fileKey = String.format("banners/%s/%s/%s.jpg", folder, ownerId, fileId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(bufferedImage)
                .size(1200, 400)
                .crop(Positions.CENTER)
                .outputFormat("jpg")
                .outputQuality(0.88)
                .toOutputStream(out);

        byte[] bannerBytes = out.toByteArray();
        return storageService.uploadFile(fileKey, bannerBytes, "image/jpeg");
    }

    private BufferedImage ensureRgb(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = rgbImage.createGraphics();
        try {
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.drawImage(image, 0, 0, null);
        } finally {
            g2d.dispose();
        }
        return rgbImage;
    }

    private void validateImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or missing");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 25MB limit");
        }

        String detectedMime = tika.detect(file.getInputStream());
        if (!ALLOWED_MIME_TYPES.contains(detectedMime)) {
            throw new BadRequestException("Unsupported image format. Allowed formats: JPEG, PNG, WEBP");
        }
    }
}