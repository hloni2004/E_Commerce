package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.ProductColourSize;
import za.ac.styling.service.ProductColourSizeService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-colour-sizes")
public class ProductColourSizeController {

    private ProductColourSizeService productColourSizeService;

    @Autowired
    public void setProductColourSizeService(ProductColourSizeService productColourSizeService) {
        this.productColourSizeService = productColourSizeService;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductColourSize> createProductColourSize(@RequestBody ProductColourSize productColourSize) {
        try {
            ProductColourSize created = productColourSizeService.create(productColourSize);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Integer id) {
        try {
            ProductColourSize productColourSize = productColourSizeService.read(id);
            if (productColourSize == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Product colour size not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", productColourSize));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving product colour size: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody ProductColourSize productColourSize) {
        try {
            ProductColourSize updated = productColourSizeService.update(productColourSize);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Product colour size not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error updating product colour size: " + e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<ProductColourSize> productColourSizes = productColourSizeService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", productColourSizes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving product colour sizes: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProductColourSize(@PathVariable Integer id) {
        try {
            productColourSizeService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Product colour size deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting product colour size: " + e.getMessage()));
        }
    }

    @GetMapping("/colour/{colourId}")
    public ResponseEntity<?> getSizesByColour(@PathVariable Integer colourId) {
        try {
            List<ProductColourSize> sizes = productColourSizeService.findByColourId(colourId);
            return ResponseEntity.ok(Map.of("success", true, "data", sizes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving sizes: " + e.getMessage()));
        }
    }

    @PutMapping("/update-stock/{id}")
    public ResponseEntity<?> updateStock(@PathVariable Integer id, @RequestParam int stock) {
        try {
            ProductColourSize updated = productColourSizeService.addStock(id, stock);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Product colour size not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", updated, "message", "Stock updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error updating stock: " + e.getMessage()));
        }
    }

    @GetMapping("/check-stock/{id}")
    public ResponseEntity<?> checkStock(@PathVariable Integer id) {
        try {
            ProductColourSize productColourSize = productColourSizeService.read(id);
            if (productColourSize == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Product colour size not found"));
            }
            boolean inStock = productColourSize.getStockQuantity() > 0;
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "inStock", inStock, 
                "stock", productColourSize.getStockQuantity()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error checking stock: " + e.getMessage()));
        }
    }
}
