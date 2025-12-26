package za.ac.styling.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling file uploads to Supabase Storage
 */
@Slf4j
@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service.key}")
    private String serviceRoleKey;

    @Value("${supabase.bucket.product.images:product-images}")
    private String productImagesBucket;

    @Value("${supabase.bucket.review.images:review-images}")
    private String reviewImagesBucket;

    @Value("${supabase.bucket.category.images:category-images}")
    private String categoryImagesBucket;

    /**
     * Upload category image
     */
    public UploadResult uploadCategoryImage(MultipartFile file, Long categoryId) throws IOException {
        String folder = "categories/" + categoryId;
        try {
            return uploadFile(file, categoryImagesBucket, folder);
        } catch (IOException e) {
            // If the configured category bucket doesn't exist on Supabase, fall back to
            // productImagesBucket
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("bucket not found") || msg.contains("bucket") || msg.contains("not found")) {
                log.warn("Category bucket '{}' not found. Falling back to product bucket '{}'. Error: {}",
                        categoryImagesBucket, productImagesBucket, e.getMessage());
                return uploadFile(file, productImagesBucket, "categories/" + categoryId);
            }
            throw e;
        }
    }

    private final OkHttpClient httpClient;

    public SupabaseStorageService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Upload a file to Supabase Storage
     * 
     * @param file   MultipartFile to upload
     * @param bucket Bucket name (product-images or review-images)
     * @param folder Optional folder path within bucket
     * @return UploadResult containing URL and path
     */
    public UploadResult uploadFile(MultipartFile file, String bucket, String folder) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        // Build path: folder/filename or just filename
        String path = folder != null && !folder.isEmpty()
                ? folder + "/" + filename
                : filename;

        // Build Supabase Storage URL
        String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                supabaseUrl, bucket, path);

        // Create request body
        RequestBody requestBody = RequestBody.create(
                file.getBytes(),
                MediaType.parse(contentType));

        // Build request
        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + serviceRoleKey)
                .addHeader("Content-Type", contentType)
                .addHeader("x-upsert", "true") // Allow overwrite
                .build();

        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("❌ Supabase upload FAILED - Bucket: {}, Path: {}, Status: {}, Error: {}",
                        bucket, path, response.code(), errorBody);
                throw new IOException("Upload failed: " + response.code() + " - " + errorBody);
            }

            // Build public URL
            String publicUrl = String.format("%s/storage/v1/object/public/%s/%s",
                    supabaseUrl, bucket, path);

            log.info("✅ Supabase upload SUCCESS - File: {}, Size: {} bytes, URL: {}",
                    file.getOriginalFilename(), file.getSize(), publicUrl);

            return new UploadResult(publicUrl, path, bucket);
        }
    }

    /**
     * Upload product image
     */
    public UploadResult uploadProductImage(MultipartFile file, Integer productId) throws IOException {
        String folder = "products/" + productId;
        return uploadFile(file, productImagesBucket, folder);
    }

    /**
     * Upload review image
     */
    public UploadResult uploadReviewImage(MultipartFile file, Long reviewId) throws IOException {
        String folder = "reviews/" + reviewId;
        return uploadFile(file, reviewImagesBucket, folder);
    }

    /**
     * Delete a file from Supabase Storage
     * 
     * @param bucket Bucket name
     * @param path   File path in bucket
     */
    public void deleteFile(String bucket, String path) throws IOException {
        String deleteUrl = String.format("%s/storage/v1/object/%s/%s",
                supabaseUrl, bucket, path);

        Request request = new Request.Builder()
                .url(deleteUrl)
                .delete()
                .addHeader("Authorization", "Bearer " + serviceRoleKey)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() && response.code() != 404) {
                // 404 is OK (file already deleted)
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Delete failed: " + response.code() + " - " + errorBody);
            }
        }
    }

    /**
     * Delete product image
     */
    public void deleteProductImage(String path) throws IOException {
        deleteFile(productImagesBucket, path);
    }

    /**
     * Delete review image
     */
    public void deleteReviewImage(String path) throws IOException {
        deleteFile(reviewImagesBucket, path);
    }

    /**
     * Generate a signed URL for private access (valid for 1 hour)
     * Use this for authenticated/private images
     */
    public String getSignedUrl(String bucket, String path) throws IOException {
        String signedUrl = String.format("%s/storage/v1/object/sign/%s/%s",
                supabaseUrl, bucket, path);

        // Create JSON body for expiry time (3600 seconds = 1 hour)
        String jsonBody = "{\"expiresIn\": 3600}";
        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(signedUrl)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + serviceRoleKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to generate signed URL: " + response.code());
            }

            // Parse JSON response to get signedURL field
            String responseBody = response.body().string();
            // Simple JSON parsing (you can use Jackson for more robust parsing)
            String signedUrlPath = responseBody.split("\"signedURL\":\"")[1].split("\"")[0];
            return supabaseUrl + signedUrlPath;
        }
    }

    /**
     * Result object containing upload information
     */
    public static class UploadResult {
        private final String url;
        private final String path;
        private final String bucket;

        public UploadResult(String url, String path, String bucket) {
            this.url = url;
            this.path = path;
            this.bucket = bucket;
        }

        public String getUrl() {
            return url;
        }

        public String getPath() {
            return path;
        }

        public String getBucket() {
            return bucket;
        }
    }
}
