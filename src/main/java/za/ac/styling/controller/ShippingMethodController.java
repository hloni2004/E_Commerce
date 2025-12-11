package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.ShippingMethod;
import za.ac.styling.service.ShippingMethodService;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/shipping-methods")
public class ShippingMethodController {

    private ShippingMethodService shippingMethodService;

    @Autowired
    public void setShippingMethodService(ShippingMethodService shippingMethodService) {
        this.shippingMethodService = shippingMethodService;
    }

    @PostMapping("/create")
    public ResponseEntity<ShippingMethod> createShippingMethod(@RequestBody ShippingMethod shippingMethod) {
        try {
            ShippingMethod created = shippingMethodService.create(shippingMethod);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Long id) {
        try {
            ShippingMethod shippingMethod = shippingMethodService.read(id);
            if (shippingMethod == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Shipping method not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", shippingMethod));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving shipping method: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody ShippingMethod shippingMethod) {
        try {
            ShippingMethod updated = shippingMethodService.update(shippingMethod);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Shipping method not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error updating shipping method: " + e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<ShippingMethod> shippingMethods = shippingMethodService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", shippingMethods));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving shipping methods: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteShippingMethod(@PathVariable Long id) {
        try {
            shippingMethodService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Shipping method deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting shipping method: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveShippingMethods() {
        try {
            List<ShippingMethod> methods = shippingMethodService.findActiveShippingMethods();
            return ResponseEntity.ok(Map.of("success", true, "data", methods));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving active shipping methods: " + e.getMessage()));
        }
    }
}
