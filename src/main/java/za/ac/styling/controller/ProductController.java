package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.*;
import za.ac.styling.dto.ProductCreateRequest;
import za.ac.styling.dto.ProductColourRequest;
import za.ac.styling.dto.ProductSizeRequest;
import za.ac.styling.service.ProductService;
import za.ac.styling.service.CategoryService;
import za.ac.styling.repository.CartItemRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody ProductCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate category exists
            Category category = categoryService.read(request.getCategoryId());
            if (category == null) {
                response.put("success", false);
                response.put("message", "Category not found");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Create Product
                Product product = Product.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .basePrice(request.getBasePrice())
                    .comparePrice(request.getComparePrice() != null ? request.getComparePrice() : 0.0)
                    .sku(request.getSku())
                    .weight(request.getWeight() != null ? request.getWeight() : 0.0)
                    .category(category)
                    .isActive(request.isActive())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDate.now())
                    .images(new ArrayList<>()) // Always initialize images list
                    .build();

            // Save product first to get ID
            Product savedProduct = productService.create(product);

            // Create and save images with proper FK
            if (request.getImageBase64List() != null && !request.getImageBase64List().isEmpty()) {
                List<ProductImage> images = new ArrayList<>();
                for (int i = 0; i < request.getImageBase64List().size(); i++) {
                    String base64Data = request.getImageBase64List().get(i);

                    // Remove data URL prefix if present (e.g., "data:image/png;base64,")
                    String base64Image = base64Data;
                    String contentType = "image/jpeg";

                    if (base64Data.contains(",")) {
                        String[] parts = base64Data.split(",");
                        base64Image = parts[1];

                        // Extract content type
                        if (parts[0].contains("image/")) {
                            String typeSection = parts[0];
                            if (typeSection.contains("image/png"))
                                contentType = "image/png";
                            else if (typeSection.contains("image/jpeg") || typeSection.contains("image/jpg"))
                                contentType = "image/jpeg";
                            else if (typeSection.contains("image/webp"))
                                contentType = "image/webp";
                        }
                    }

                    // TODO: Upload to Supabase Storage instead of storing as BLOB
                    // For now, skip BLOB storage - images should be uploaded via multipart/form-data
                    System.out.println("⚠️ Warning: Base64 image upload detected. Please use multipart file upload to Supabase Storage.");
                    
                    ProductImage image = ProductImage.builder()
                            .product(savedProduct)
                            .contentType(contentType)
                            .altText(savedProduct.getName())
                            .displayOrder(i)
                            .isPrimary(i == 0)
                            .build();
                    
                    // Skip adding this image since we don't have Supabase URL
                    continue;
                }
                savedProduct.setImages(images);

                // Set primary image
                if (!images.isEmpty()) {
                    savedProduct.setPrimaryImage(images.get(0));
                }
            }

            // Create and save colours with sizes with proper FK
            if (request.getColours() != null && !request.getColours().isEmpty()) {
                List<ProductColour> colours = new ArrayList<>();

                for (ProductColourRequest colourReq : request.getColours()) {
                    ProductColour colour = ProductColour.builder()
                            .name(colourReq.getName())
                            .hexCode(colourReq.getHexCode())
                            .product(savedProduct)
                            .build();

                    // Create sizes with proper FK
                    if (colourReq.getSizes() != null && !colourReq.getSizes().isEmpty()) {
                        List<ProductColourSize> sizes = new ArrayList<>();

                        for (ProductSizeRequest sizeReq : colourReq.getSizes()) {
                            ProductColourSize size = ProductColourSize.builder()
                                    .sizeName(sizeReq.getSizeName())
                                    .stockQuantity(sizeReq.getStockQuantity())
                                    .reservedQuantity(0)
                                    .reorderLevel(5)
                                    .colour(colour)
                                    .build();
                            sizes.add(size);
                        }
                        colour.setSizes(sizes);
                    }

                    colours.add(colour);
                }
                savedProduct.setColours(colours);
            }

            // Update product with all relationships
            Product finalProduct = productService.update(savedProduct);

            response.put("success", true);
            response.put("message", "Product created successfully");
            response.put("data", finalProduct);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error creating product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<?> getProductImage(@PathVariable Long imageId) {
        try {
            // Return Supabase URL instead of BLOB data
            ProductImage image = productService.getImageById(imageId);
            if (image == null) {
                return ResponseEntity.notFound().build();
            }

            // Images are stored in Supabase Storage - return the URL
            if (image.getSupabaseUrl() != null) {
                return ResponseEntity.ok(Map.of(
                    "imageUrl", image.getSupabaseUrl(),
                    "contentType", image.getContentType() != null ? image.getContentType() : "image/jpeg"
                ));
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Integer id) {
        try {
            Product product = productService.read(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Product not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving product: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody ProductCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get existing product
            Product existingProduct = productService.read(request.getProductId());
            if (existingProduct == null) {
                response.put("success", false);
                response.put("message", "Product not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Validate category exists
            Category category = categoryService.read(request.getCategoryId());
            if (category == null) {
                response.put("success", false);
                response.put("message", "Category not found");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Update basic product fields
            existingProduct.setName(request.getName());
            existingProduct.setDescription(request.getDescription());
            existingProduct.setBasePrice(request.getBasePrice());
            existingProduct.setComparePrice(request.getComparePrice() != null ? request.getComparePrice() : 0.0);
            existingProduct.setSku(request.getSku());
            existingProduct.setWeight(request.getWeight() != null ? request.getWeight() : 0.0);
            existingProduct.setCategory(category);
            existingProduct.setActive(request.isActive());
            existingProduct.setUpdatedAt(LocalDate.now());

            // Handle new images if provided
            if (request.getImageBase64List() != null && !request.getImageBase64List().isEmpty()) {
                // Initialize images collection if null
                if (existingProduct.getImages() == null) {
                    existingProduct.setImages(new ArrayList<>());
                }

                // Remove images that are not in the existingImageIds list
                if (request.getExistingImageIds() != null) {
                    existingProduct.getImages()
                            .removeIf(img -> !request.getExistingImageIds().contains(img.getImageId()));
                } else {
                    // Clear all existing images if no existing IDs provided
                    existingProduct.getImages().clear();
                }

                // Add new images
                int currentSize = existingProduct.getImages().size();
                for (int i = 0; i < request.getImageBase64List().size(); i++) {
                    String base64Data = request.getImageBase64List().get(i);
                    String base64Image = base64Data;
                    String contentType = "image/jpeg";

                    if (base64Data.contains(",")) {
                        String[] parts = base64Data.split(",");
                        base64Image = parts[1];
                        if (parts[0].contains("image/png"))
                            contentType = "image/png";
                        else if (parts[0].contains("image/webp"))
                            contentType = "image/webp";
                    }

                    // TODO: Upload to Supabase Storage instead of storing as BLOB
                    // For now, skip BLOB storage - images should be uploaded via multipart/form-data
                    System.out.println("⚠️ Warning: Base64 image upload detected. Please use multipart file upload to Supabase Storage.");
                    
                    ProductImage image = ProductImage.builder()
                            .product(existingProduct)
                            .contentType(contentType)
                            .altText(existingProduct.getName())
                            .displayOrder(currentSize + i)
                            .isPrimary(existingProduct.getImages().isEmpty() && i == 0)
                            .build();
                    
                    // Skip adding this image since we don't have Supabase URL
                    continue;
                }

                // Set primary image
                if (!existingProduct.getImages().isEmpty()) {
                    existingProduct.setPrimaryImage(existingProduct.getImages().stream()
                            .filter(ProductImage::isPrimary)
                            .findFirst()
                            .orElse(existingProduct.getImages().get(0)));
                }
            }

            // Update colours and sizes
            if (request.getColours() != null && !request.getColours().isEmpty()) {
                // Clear existing colours properly to avoid orphan removal issue
                if (existingProduct.getColours() != null) {
                    existingProduct.getColours().clear();
                } else {
                    existingProduct.setColours(new ArrayList<>());
                }

                for (ProductColourRequest colourReq : request.getColours()) {
                    ProductColour colour = ProductColour.builder()
                            .product(existingProduct)
                            .name(colourReq.getName())
                            .hexCode(colourReq.getHexCode())
                            .build();

                    // Add sizes
                    if (colourReq.getSizes() != null && !colourReq.getSizes().isEmpty()) {
                        List<ProductColourSize> sizes = new ArrayList<>();

                        for (ProductSizeRequest sizeReq : colourReq.getSizes()) {
                            ProductColourSize size = ProductColourSize.builder()
                                    .colour(colour)
                                    .sizeName(sizeReq.getSizeName())
                                    .stockQuantity(sizeReq.getStockQuantity())
                                    .reservedQuantity(0)
                                    .reorderLevel(5)
                                    .build();

                            sizes.add(size);
                        }
                        colour.setSizes(sizes);
                    }

                    // Add to existing collection instead of replacing
                    existingProduct.getColours().add(colour);
                }
            }

            // Save updated product
            Product updatedProduct = productService.update(existingProduct);

            response.put("success", true);
            response.put("message", "Product updated successfully");
            response.put("data", updatedProduct);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error updating product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<Product> products = productService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving products: " + e.getMessage()));
        }
    }

    @Autowired
    private CartItemRepository cartItemRepository;

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            Product product = productService.read(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Product not found"));
            }

            // First, delete all cart items that reference this product's colours/sizes
            // This prevents FK constraint violations
            if (product.getColours() != null) {
                for (ProductColour colour : product.getColours()) {
                    if (colour.getSizes() != null) {
                        for (ProductColourSize size : colour.getSizes()) {
                            cartItemRepository.deleteBySize(size);
                        }
                    }
                    cartItemRepository.deleteByColour(colour);
                }
            }
            cartItemRepository.deleteByProduct(product);

            // Remove primaryImage reference to avoid FK constraint issues
            product.setPrimaryImage(null);
            productService.update(product);

            // Now delete the product (cascade will handle images, colours, sizes)
            productService.delete(id);

            return ResponseEntity.ok(Map.of("success", true, "message", "Product deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error deleting product: " + e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable Long categoryId) {
        try {
            List<Product> products = productService.findByCategoryId(categoryId);
            return ResponseEntity.ok(Map.of("success", true, "data", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving products: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam(required = false) String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of("success", true, "data", Collections.emptyList()));
            }

            List<Product> products = productService.searchByName(query.trim());
            return ResponseEntity.ok(Map.of("success", true, "data", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error searching products: " + e.getMessage()));
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockProducts() {
        try {
            List<Product> allProducts = productService.getAll();
            List<Map<String, Object>> lowStockAlerts = new ArrayList<>();

            if (allProducts != null) {
                for (Product product : allProducts) {
                    if (product.getColours() != null && !product.getColours().isEmpty()) {
                        for (ProductColour colour : product.getColours()) {
                            if (colour.getSizes() != null && !colour.getSizes().isEmpty()) {
                                for (ProductColourSize size : colour.getSizes()) {
                                    // Check if stock is below reorder level
                                    int reorderLevel = size.getReorderLevel() > 0 ? size.getReorderLevel() : 20;
                                    int currentStock = size.getStockQuantity();

                                    if (currentStock < reorderLevel) {
                                        Map<String, Object> alert = new HashMap<>();
                                        alert.put("id",
                                                product.getProductId() * 10000
                                                        + (colour.getColourId() != null ? colour.getColourId() : 0)
                                                                * 100
                                                        + (size.getSizeId() != null ? size.getSizeId() : 0));
                                        alert.put("name", product.getName());
                                        alert.put("sku", product.getSku());
                                        alert.put("currentStock", currentStock);
                                        alert.put("reorderLevel", reorderLevel);
                                        alert.put("color", colour.getName());
                                        alert.put("size", size.getSizeName());
                                        lowStockAlerts.add(alert);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return ResponseEntity.ok(Map.of("success", true, "data", lowStockAlerts));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error fetching low stock alerts: " + e.getMessage()));
        }
    }
}
