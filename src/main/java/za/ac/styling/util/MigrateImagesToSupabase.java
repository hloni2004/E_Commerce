package za.ac.styling.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import za.ac.styling.domain.ProductImage;
import za.ac.styling.domain.ReviewImage;
import za.ac.styling.service.ProductImageService;
import za.ac.styling.service.SupabaseStorageService;
import za.ac.styling.repository.ReviewImageRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * One-time migration script to move existing BLOB images to Supabase
 * 
 * USAGE:
 * 1. Uncomment @Component annotation below
 * 2. Restart the application
 * 3. Wait for migration to complete
 * 4. Comment out @Component again to prevent re-running
 * 5. Optional: Remove image_data columns from database after verifying
 */
// @Component  // UNCOMMENT TO RUN MIGRATION
public class MigrateImagesToSupabase implements CommandLineRunner {

    @Autowired
    private ProductImageService productImageService;
    
    @Autowired
    private ReviewImageRepository reviewImageRepository;
    
    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 STARTING BLOB TO SUPABASE MIGRATION");
        System.out.println("=".repeat(80) + "\n");
        
        migrateProductImages();
        migrateReviewImages();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ MIGRATION COMPLETED!");
        System.out.println("=".repeat(80) + "\n");
        System.out.println("Next steps:");
        System.out.println("1. Verify images are loading from Supabase URLs");
        System.out.println("2. Comment out @Component annotation in MigrateImagesToSupabase.java");
        System.out.println("3. Restart application to prevent re-running migration");
        System.out.println("4. Optional: Run SQL to remove image_data columns:");
        System.out.println("   ALTER TABLE product_image DROP COLUMN image_data;");
        System.out.println("   ALTER TABLE review_image DROP COLUMN image_data;");
        System.out.println();
    }

    private void migrateProductImages() {
        System.out.println("📦 Migrating Product Images...");
        
        List<ProductImage> allImages = productImageService.getAll();
        int migrated = 0;
        int skipped = 0;
        int failed = 0;

        for (ProductImage image : allImages) {
            try {
                // Skip if already migrated
                if (image.isSupabaseImage()) {
                    System.out.println("  ⏭️  Skipped: Image " + image.getImageId() + " (already in Supabase)");
                    skipped++;
                    continue;
                }

                // Skip if no BLOB data
                if (!image.isBlobImage()) {
                    System.out.println("  ⏭️  Skipped: Image " + image.getImageId() + " (no data)");
                    skipped++;
                    continue;
                }

                // Create MultipartFile from BLOB data
                byte[] imageData = image.getImageData();
                String contentType = image.getContentType() != null ? image.getContentType() : "image/jpeg";
                
                MultipartFile multipartFile = new ByteArrayMultipartFile(
                    imageData,
                    "image-" + image.getImageId(),
                    contentType
                );

                // Upload to Supabase
                Integer productId = image.getProduct().getProductId();
                SupabaseStorageService.UploadResult result = 
                    supabaseStorageService.uploadProductImage(multipartFile, productId);

                // Update database record
                image.setSupabaseUrl(result.getUrl());
                image.setBucketPath(result.getPath());
                // Keep image_data for now (remove later after verification)
                productImageService.update(image);

                System.out.println("  ✅ Migrated: Image " + image.getImageId() + 
                    " (Product " + productId + ") → " + result.getUrl());
                migrated++;

            } catch (Exception e) {
                System.err.println("  ❌ Failed: Image " + image.getImageId() + " - " + e.getMessage());
                failed++;
            }
        }

        System.out.println("\n📊 Product Images Summary:");
        System.out.println("   ✅ Migrated: " + migrated);
        System.out.println("   ⏭️  Skipped: " + skipped);
        System.out.println("   ❌ Failed: " + failed);
        System.out.println();
    }

    private void migrateReviewImages() {
        System.out.println("📦 Migrating Review Images...");
        
        List<ReviewImage> allImages = reviewImageRepository.findAll();
        int migrated = 0;
        int skipped = 0;
        int failed = 0;

        for (ReviewImage image : allImages) {
            try {
                // Skip if already migrated
                if (image.isSupabaseImage()) {
                    System.out.println("  ⏭️  Skipped: ReviewImage " + image.getImageId() + " (already in Supabase)");
                    skipped++;
                    continue;
                }

                // Skip if no BLOB data
                if (image.getImageData() == null || image.getImageData().length == 0) {
                    System.out.println("  ⏭️  Skipped: ReviewImage " + image.getImageId() + " (no data)");
                    skipped++;
                    continue;
                }

                // Create MultipartFile from BLOB data
                byte[] imageData = image.getImageData();
                String contentType = image.getContentType() != null ? image.getContentType() : "image/jpeg";
                
                MultipartFile multipartFile = new ByteArrayMultipartFile(
                    imageData,
                    "review-image-" + image.getImageId(),
                    contentType
                );

                // Upload to Supabase
                Integer reviewIdInt = image.getReview().getReviewId();
                Long reviewId = reviewIdInt != null ? reviewIdInt.longValue() : 0L;
                SupabaseStorageService.UploadResult result = 
                    supabaseStorageService.uploadReviewImage(multipartFile, reviewId);

                // Update database record
                image.setSupabaseUrl(result.getUrl());
                image.setBucketPath(result.getPath());
                // Keep image_data for now
                reviewImageRepository.save(image);

                System.out.println("  ✅ Migrated: ReviewImage " + image.getImageId() + 
                    " (Review " + reviewId + ") → " + result.getUrl());
                migrated++;

            } catch (Exception e) {
                System.err.println("  ❌ Failed: ReviewImage " + image.getImageId() + " - " + e.getMessage());
                failed++;
            }
        }

        System.out.println("\n📊 Review Images Summary:");
        System.out.println("   ✅ Migrated: " + migrated);
        System.out.println("   ⏭️  Skipped: " + skipped);
        System.out.println("   ❌ Failed: " + failed);
        System.out.println();
    }

    /**
     * Helper class to convert byte[] to MultipartFile
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] data;
        private final String name;
        private final String contentType;

        public ByteArrayMultipartFile(byte[] data, String name, String contentType) {
            this.data = data;
            this.name = name;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return data == null || data.length == 0;
        }

        @Override
        public long getSize() {
            return data.length;
        }

        @Override
        public byte[] getBytes() {
            return data;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(data);
        }

        @Override
        public void transferTo(java.io.File dest) throws java.io.IOException {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
