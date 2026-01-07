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

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public ProductController(ProductService productService, CategoryService categoryService, CartItemRepository cartItemRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartItemRepository = cartItemRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody ProductCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {

            Category category = categoryService.read(request.getCategoryId());
            if (category == null) {
                response.put("success", false);
                response.put("message", "Category not found");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Product product = Product.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .basePrice(request.getBasePrice())
                    .comparePrice(request.getComparePrice())
                    .sku(request.getSku())
                    .weight(request.getWeight() != null ? request.getWeight() : 0.0)
                    .category(category)
                    .isActive(request.isActive())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDate.now())
                    .images(new HashSet<>())
                    .build();

            Product savedProduct = productService.create(product);

            if (request.getImageBase64List() != null && !request.getImageBase64List().isEmpty()) {
                Set<ProductImage> images = new HashSet<>();
                for (int i = 0; i < request.getImageBase64List().size(); i++) {
                    String base64Data = request.getImageBase64List().get(i);

                    String base64Image = base64Data;
                    String contentType = "image/jpeg";

                    if (base64Data.contains(",")) {
                        String[] parts = base64Data.split(",");
                        base64Image = parts[1];

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

                    System.out.println(
                            "⚠️ Warning: Base64 image upload detected. Please use multipart file upload to Supabase Storage.");

                    ProductImage image = ProductImage.builder()
                            .product(savedProduct)
                            .contentType(contentType)
                            .altText(savedProduct.getName())
                            .displayOrder(i)
                            .isPrimary(i == 0)
                            .build();

                    continue;
                }
                savedProduct.setImages(images);

                if (!images.isEmpty()) {
                    savedProduct.setPrimaryImage(images.iterator().next());
                }
            }

            if (request.getColours() != null && !request.getColours().isEmpty()) {
                Set<ProductColour> colours = new HashSet<>();

                for (ProductColourRequest colourReq : request.getColours()) {
                    ProductColour colour = ProductColour.builder()
                            .name(colourReq.getName())
                            .hexCode(colourReq.getHexCode())
                            .product(savedProduct)
                            .build();

                    if (colourReq.getSizes() != null && !colourReq.getSizes().isEmpty()) {
                        Set<ProductColourSize> sizes = new HashSet<>();

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

            ProductImage image = productService.getImageById(imageId);
            if (image == null) {
                return ResponseEntity.notFound().build();
            }

            if (image.getSupabaseUrl() != null) {
                return ResponseEntity.ok(Map.of(
                        "imageUrl", image.getSupabaseUrl(),
                        "contentType", image.getContentType() != null ? image.getContentType() : "image/jpeg"));
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
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving product: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody ProductCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {

            Product existingProduct = productService.read(request.getProductId());
            if (existingProduct == null) {
                response.put("success", false);
                response.put("message", "Product not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Category category = categoryService.read(request.getCategoryId());
            if (category == null) {
                response.put("success", false);
                response.put("message", "Category not found");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            existingProduct.setName(request.getName());
            existingProduct.setDescription(request.getDescription());
            existingProduct.setBasePrice(request.getBasePrice());
            existingProduct.setComparePrice(request.getComparePrice());
            existingProduct.setSku(request.getSku());
            existingProduct.setWeight(request.getWeight() != null ? request.getWeight() : 0.0);
            existingProduct.setCategory(category);
            existingProduct.setActive(request.isActive());
            existingProduct.setUpdatedAt(LocalDate.now());

            if (request.getImageBase64List() != null && !request.getImageBase64List().isEmpty()) {

                if (existingProduct.getImages() == null) {
                    existingProduct.setImages(new HashSet<>());
                }

                if (request.getExistingImageIds() != null) {
                    existingProduct.getImages()
                            .removeIf(img -> !request.getExistingImageIds().contains(img.getImageId()));
                } else {

                    existingProduct.getImages().clear();
                }

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

                    System.out.println(
                            "⚠️ Warning: Base64 image upload detected. Please use multipart file upload to Supabase Storage.");

                    ProductImage image = ProductImage.builder()
                            .product(existingProduct)
                            .contentType(contentType)
                            .altText(existingProduct.getName())
                            .displayOrder(currentSize + i)
                            .isPrimary(existingProduct.getImages().isEmpty() && i == 0)
                            .build();

                    continue;
                }

                if (!existingProduct.getImages().isEmpty()) {
                    existingProduct.setPrimaryImage(existingProduct.getImages().stream()
                            .filter(ProductImage::isPrimary)
                            .findFirst()
                            .orElse(existingProduct.getImages().iterator().next()));
                }
            }

            if (request.getColours() != null && !request.getColours().isEmpty()) {

                if (existingProduct.getColours() == null) {
                    existingProduct.setColours(new HashSet<>());
                }

                Set<String> newColourKeys = new HashSet<>();
                for (ProductColourRequest colourReq : request.getColours()) {
                    String colourKey = colourReq.getName() + "|" + colourReq.getHexCode();
                    newColourKeys.add(colourKey);
                }

                existingProduct.getColours().removeIf(existingColour -> {
                    String existingKey = existingColour.getName() + "|" + existingColour.getHexCode();
                    return !newColourKeys.contains(existingKey);
                });

                for (ProductColourRequest colourReq : request.getColours()) {
                    String colourKey = colourReq.getName() + "|" + colourReq.getHexCode();

                    ProductColour existingColour = existingProduct.getColours().stream()
                        .filter(c -> (c.getName() + "|" + c.getHexCode()).equals(colourKey))
                        .findFirst()
                        .orElse(null);

                    if (existingColour != null) {

                        if (existingColour.getSizes() == null) {
                            existingColour.setSizes(new HashSet<>());
                        }

                        Set<String> newSizeNames = new HashSet<>();
                        if (colourReq.getSizes() != null) {
                            for (ProductSizeRequest sizeReq : colourReq.getSizes()) {
                                newSizeNames.add(sizeReq.getSizeName());
                            }
                        }

                        existingColour.getSizes().removeIf(existingSize -> 
                            !newSizeNames.contains(existingSize.getSizeName())
                        );

                        if (colourReq.getSizes() != null) {
                            for (ProductSizeRequest sizeReq : colourReq.getSizes()) {
                                ProductColourSize existingSize = existingColour.getSizes().stream()
                                    .filter(s -> s.getSizeName().equals(sizeReq.getSizeName()))
                                    .findFirst()
                                    .orElse(null);

                                if (existingSize != null) {

                                    existingSize.setStockQuantity(sizeReq.getStockQuantity());
                                } else {

                                    ProductColourSize newSize = ProductColourSize.builder()
                                        .colour(existingColour)
                                        .sizeName(sizeReq.getSizeName())
                                        .stockQuantity(sizeReq.getStockQuantity())
                                        .reservedQuantity(0)
                                        .reorderLevel(5)
                                        .build();
                                    existingColour.getSizes().add(newSize);
                                }
                            }
                        }
                    } else {

                        ProductColour newColour = ProductColour.builder()
                            .product(existingProduct)
                            .name(colourReq.getName())
                            .hexCode(colourReq.getHexCode())
                            .build();

                        if (colourReq.getSizes() != null && !colourReq.getSizes().isEmpty()) {
                            Set<ProductColourSize> sizes = new HashSet<>();
                            for (ProductSizeRequest sizeReq : colourReq.getSizes()) {
                                ProductColourSize size = ProductColourSize.builder()
                                    .colour(newColour)
                                    .sizeName(sizeReq.getSizeName())
                                    .stockQuantity(sizeReq.getStockQuantity())
                                    .reservedQuantity(0)
                                    .reorderLevel(5)
                                    .build();
                                sizes.add(size);
                            }
                            newColour.setSizes(sizes);
                        }
                        existingProduct.getColours().add(newColour);
                    }
                }
            }

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
            List<Product> products = productService.getAllWithRelations();
            return ResponseEntity.ok(Map.of("success", true, "data", products));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving products: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllIncludingDeleted() {
        try {
            List<Product> products = productService.getAllIncludingDeleted();
            return ResponseEntity.ok(Map.of("success", true, "data", products));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving products: " + e.getMessage()));
        }
    }

    @PostMapping("/restore/{id}")
    public ResponseEntity<?> restoreProduct(@PathVariable Integer id) {
        try {
            Product p = productService.restoreProduct(id);
            if (p == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Product not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "Product restored", "data", p));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error restoring product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            Product product = productService.read(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Product not found"));
            }

            product.softDelete();
            productService.update(product);

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
            List<Product> products = productService.findByCategoryIdWithRelations(categoryId);
            return ResponseEntity.ok(Map.of("success", true, "data", products));
        } catch (Exception e) {
            e.printStackTrace();
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
            List<Product> allProducts = productService.getAllWithRelations();
            List<Map<String, Object>> lowStockAlerts = new ArrayList<>();

            if (allProducts != null) {
                for (Product product : allProducts) {
                    if (product.getColours() != null && !product.getColours().isEmpty()) {
                        for (ProductColour colour : product.getColours()) {
                            if (colour.getSizes() != null && !colour.getSizes().isEmpty()) {
                                for (ProductColourSize size : colour.getSizes()) {

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
