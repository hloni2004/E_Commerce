package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.ProductColour;
import za.ac.styling.service.ProductColourService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-colours")
public class ProductColourController {

    private ProductColourService productColourService;

    @Autowired
    public void setProductColourService(ProductColourService productColourService) {
        this.productColourService = productColourService;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductColour> createProductColour(@RequestBody ProductColour productColour) {
        try {
            ProductColour created = productColourService.create(productColour);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Integer id) {
        try {
            ProductColour productColour = productColourService.read(id);
            if (productColour == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Product colour not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", productColour));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving product colour: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody ProductColour productColour) {
        try {
            ProductColour updated = productColourService.update(productColour);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Product colour not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error updating product colour: " + e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<ProductColour> productColours = productColourService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", productColours));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving product colours: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProductColour(@PathVariable Integer id) {
        try {
            productColourService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Product colour deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting product colour: " + e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getColoursByProduct(@PathVariable Integer productId) {
        try {
            List<ProductColour> colours = productColourService.findByProductId(productId);
            return ResponseEntity.ok(Map.of("success", true, "data", colours));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving product colours: " + e.getMessage()));
        }
    }
}
