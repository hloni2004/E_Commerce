package za.ac.styling.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    public UploadResult uploadCategoryImage(MultipartFile file, Long categoryId) throws IOException {
        String folder = "categories/" + categoryId;
        try {
            return uploadFile(file, categoryImagesBucket, folder);
        } catch (IOException e) {

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

    public UploadResult uploadFile(MultipartFile file, String bucket, String folder) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        String path = folder != null && !folder.isEmpty()
                ? folder + "/" + filename
                : filename;

        String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                supabaseUrl, bucket, path);

        RequestBody requestBody = RequestBody.create(
                file.getBytes(),
                MediaType.parse(contentType));

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + serviceRoleKey)
                .addHeader("Content-Type", contentType)
                .addHeader("x-upsert", "true")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("❌ Supabase upload FAILED - Bucket: {}, Path: {}, Status: {}, Error: {}",
                        bucket, path, response.code(), errorBody);
                throw new IOException("Upload failed: " + response.code() + " - " + errorBody);
            }

            String publicUrl = String.format("%s/storage/v1/object/public/%s/%s",
                    supabaseUrl, bucket, path);

            log.info("✅ Supabase upload SUCCESS - File: {}, Size: {} bytes, URL: {}",
                    file.getOriginalFilename(), file.getSize(), publicUrl);

            return new UploadResult(publicUrl, path, bucket);
        }
    }

    public UploadResult uploadProductImage(MultipartFile file, Integer productId) throws IOException {
        String folder = "products/" + productId;
        return uploadFile(file, productImagesBucket, folder);
    }

    public UploadResult uploadReviewImage(MultipartFile file, Long reviewId) throws IOException {
        String folder = "reviews/" + reviewId;
        return uploadFile(file, reviewImagesBucket, folder);
    }

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

                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Delete failed: " + response.code() + " - " + errorBody);
            }
        }
    }

    public void deleteProductImage(String path) throws IOException {
        deleteFile(productImagesBucket, path);
    }

    public void deleteReviewImage(String path) throws IOException {
        deleteFile(reviewImagesBucket, path);
    }

    public String getSignedUrl(String bucket, String path) throws IOException {
        String signedUrl = String.format("%s/storage/v1/object/sign/%s/%s",
                supabaseUrl, bucket, path);

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

            String responseBody = response.body().string();

            String signedUrlPath = responseBody.split("\"signedURL\":\"")[1].split("\"")[0];
            return supabaseUrl + signedUrlPath;
        }
    }

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
