package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductImage;
import za.ac.styling.service.ProductImageService;
import za.ac.styling.service.ProductService;
import za.ac.styling.service.SupabaseStorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {

    private ProductImageService productImageService;
    private ProductService productService;
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    public void setProductImageService(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setSupabaseStorageService(SupabaseStorageService supabaseStorageService) {
        this.supabaseStorageService = supabaseStorageService;
    }

    /**
     * Upload product images to Supabase Storage
     * Accepts multipart file upload
     */
    @PostMapping("/upload/{productId}")
    public ResponseEntity<?> uploadProductImages(
            @PathVariable Integer productId,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            System.out.println("📤 Uploading " + files.size() + " images for product " + productId);
            
            Product product = productService.read(productId);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Product not found"));
            }

            List<ProductImage> uploadedImages = new ArrayList<>();
            int displayOrder = product.getImages() != null ? product.getImages().size() : 0;

            for (MultipartFile file : files) {
                // Upload to Supabase Storage
                SupabaseStorageService.UploadResult result = 
                    supabaseStorageService.uploadProductImage(file, productId);

                // Create ProductImage entity
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .supabaseUrl(result.getUrl())
                        .bucketPath(result.getPath())
                        .contentType(file.getContentType())
                        .altText(product.getName())
                        .displayOrder(displayOrder++)
                        .isPrimary(product.getImages() == null || product.getImages().isEmpty())
                        .build();

                ProductImage saved = productImageService.create(image);
                uploadedImages.add(saved);
                
                System.out.println("✅ Uploaded: " + result.getUrl());
            }

            // Set first image as primary if no primary exists
            if (product.getPrimaryImage() == null && !uploadedImages.isEmpty()) {
                product.setPrimaryImage(uploadedImages.get(0));
                productService.update(product);
            }

            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Images uploaded successfully",
                "images", uploadedImages
            ));

        } catch (Exception e) {
            System.err.println("❌ Error uploading images: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error uploading images: " + e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ProductImage> createProductImage(@RequestBody ProductImage productImage) {
        try {
            ProductImage created = productImageService.create(productImage);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Long id) {
        try {
            ProductImage productImage = productImageService.read(id);
            if (productImage == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Product image not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", productImage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving product image: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody ProductImage productImage) {
        try {
            ProductImage updated = productImageService.update(productImage);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Product image not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error updating product image: " + e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<ProductImage> productImages = productImageService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", productImages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving product images: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long id) {
        try {
            productImageService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Product image deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting product image: " + e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getImagesByProduct(@PathVariable Integer productId) {
        try {
            List<ProductImage> images = productImageService.findByProductId(productId);
            return ResponseEntity.ok(Map.of("success", true, "data", images));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving product images: " + e.getMessage()));
        }
    }
}
