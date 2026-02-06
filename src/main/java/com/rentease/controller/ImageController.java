package com.rentease.controller;

import com.rentease.dto.response.ApiResponse;
import com.rentease.security.CurrentUser;
import com.rentease.security.UserPrincipal;
import com.rentease.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/listings")
    public ResponseEntity<ApiResponse<String>> uploadListingImage(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = imageService.uploadListingImage(file);
        return ResponseEntity.ok(ApiResponse.success(imageUrl, "Image uploaded successfully"));
    }

    @PostMapping("/listings/multiple")
    public ResponseEntity<ApiResponse<List<String>>> uploadMultipleListingImages(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("files") MultipartFile[] files) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String imageUrl = imageService.uploadListingImage(file);
            imageUrls.add(imageUrl);
        }
        return ResponseEntity.ok(ApiResponse.success(imageUrls, "Images uploaded successfully"));
    }

    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = imageService.uploadAvatarImage(file);
        return ResponseEntity.ok(ApiResponse.success(imageUrl, "Avatar uploaded successfully"));
    }

    @PostMapping("/id-document")
    public ResponseEntity<ApiResponse<String>> uploadIdDocument(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = imageService.uploadIdDocument(file);
        return ResponseEntity.ok(ApiResponse.success(imageUrl, "Document uploaded successfully"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("url") String imageUrl) {
        imageService.deleteImage(imageUrl);
        return ResponseEntity.ok(ApiResponse.success(null, "Image deleted successfully"));
    }
}
