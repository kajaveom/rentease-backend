package com.rentease.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rentease.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public String uploadImage(MultipartFile file, String folder) {
        validateFile(file);

        try {
            String publicId = folder + "/" + UUID.randomUUID().toString();

            Map<String, Object> options = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", "rentease",
                    "resource_type", "image"
            );

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            String secureUrl = (String) result.get("secure_url");

            log.info("Image uploaded successfully: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("Failed to upload image", e);
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }

    public String uploadListingImage(MultipartFile file) {
        return uploadImage(file, "listings");
    }

    public String uploadAvatarImage(MultipartFile file) {
        return uploadImage(file, "avatars");
    }

    public String uploadIdDocument(MultipartFile file) {
        return uploadImage(file, "id-documents");
    }

    public void deleteImage(String imageUrl) {
        try {
            // Extract public ID from URL
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deleted successfully: {}", publicId);
            }
        } catch (IOException e) {
            log.error("Failed to delete image: {}", imageUrl, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid file type. Allowed types: JPEG, PNG, WebP, GIF");
        }
    }

    private String extractPublicId(String imageUrl) {
        // Cloudinary URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String pathWithVersion = parts[1];
                // Remove version prefix (v1234567890/)
                String path = pathWithVersion.replaceFirst("v\\d+/", "");
                // Remove file extension
                int lastDot = path.lastIndexOf('.');
                if (lastDot > 0) {
                    return path.substring(0, lastDot);
                }
                return path;
            }
        } catch (Exception e) {
            log.warn("Could not extract public ID from URL: {}", imageUrl);
        }
        return null;
    }
}
