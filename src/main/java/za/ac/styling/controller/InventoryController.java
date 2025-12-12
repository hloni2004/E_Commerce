package za.ac.styling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.ProductColourSize;
import za.ac.styling.service.InventoryService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for real-time inventory management
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Get available stock for a specific size
     */
    @GetMapping("/available-stock/{sizeId}")
    public ResponseEntity<?> getAvailableStock(@PathVariable Integer sizeId) {
        try {
            int availableStock = inventoryService.getAvailableStock(sizeId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "sizeId", sizeId,
                    "availableStock", availableStock,
                    "inStock", availableStock > 0
                ),
                "message", "Real-time stock retrieved"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Error retrieving stock: " + e.getMessage()));
        }
    }

    /**
     * Get all low stock items (at or below reorder level)
     */
    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockItems() {
        try {
            List<ProductColourSize> lowStockItems = inventoryService.getLowStockItems();
            
            List<Map<String, Object>> response = lowStockItems.stream()
                .map(size -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("sizeId", size.getSizeId());
                    item.put("sizeName", size.getSizeName());
                    item.put("productName", size.getColour().getProduct().getName());
                    item.put("colourName", size.getColour().getName());
                    item.put("stockQuantity", size.getStockQuantity());
                    item.put("reservedQuantity", size.getReservedQuantity());
                    item.put("availableStock", size.getStockQuantity() - size.getReservedQuantity());
                    item.put("reorderLevel", size.getReorderLevel());
                    item.put("needsReorder", inventoryService.needsReorder(size));
                    return item;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("data", response);
            result.put("count", response.size());
            result.put("message", "Low stock items retrieved");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Error retrieving low stock items: " + e.getMessage()));
        }
    }

    /**
     * Get all out of stock items
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<?> getOutOfStockItems() {
        try {
            List<ProductColourSize> outOfStockItems = inventoryService.getOutOfStockItems();
            
            List<Map<String, Object>> response = outOfStockItems.stream()
                .map(size -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("sizeId", size.getSizeId());
                    item.put("sizeName", size.getSizeName());
                    item.put("productName", size.getColour().getProduct().getName());
                    item.put("colourName", size.getColour().getName());
                    item.put("stockQuantity", size.getStockQuantity());
                    item.put("reservedQuantity", size.getReservedQuantity());
                    item.put("availableStock", size.getStockQuantity() - size.getReservedQuantity());
                    return item;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("data", response);
            result.put("count", response.size());
            result.put("message", "Out of stock items retrieved");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Error retrieving out of stock items: " + e.getMessage()));
        }
    }

    /**
     * Check if multiple items have sufficient stock
     */
    @PostMapping("/check-availability")
    public ResponseEntity<?> checkStockAvailability(@RequestBody List<Map<String, Object>> items) {
        try {
            List<Map<String, Object>> availability = items.stream()
                .map(item -> {
                    Integer sizeId = (Integer) item.get("sizeId");
                    Integer requestedQuantity = (Integer) item.get("quantity");
                    int availableStock = inventoryService.getAvailableStock(sizeId);
                    
                    Map<String, Object> avail = new java.util.HashMap<>();
                    avail.put("sizeId", sizeId);
                    avail.put("requestedQuantity", requestedQuantity);
                    avail.put("availableStock", availableStock);
                    avail.put("isAvailable", availableStock >= requestedQuantity);
                    avail.put("shortfall", Math.max(0, requestedQuantity - availableStock));
                    return avail;
                })
                .collect(Collectors.toList());
            
            boolean allAvailable = availability.stream()
                .allMatch(item -> (Boolean) item.get("isAvailable"));
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("data", availability);
            result.put("allAvailable", allAvailable);
            result.put("message", "Stock availability checked in real-time");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Error checking stock availability: " + e.getMessage()));
        }
    }

    /**
     * Get real-time inventory dashboard stats
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            List<ProductColourSize> lowStock = inventoryService.getLowStockItems();
            List<ProductColourSize> outOfStock = inventoryService.getOutOfStockItems();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "lowStockCount", lowStock.size(),
                    "outOfStockCount", outOfStock.size(),
                    "lowStockItems", lowStock.stream().limit(5).collect(Collectors.toList()),
                    "outOfStockItems", outOfStock.stream().limit(5).collect(Collectors.toList())
                ),
                "message", "Real-time inventory dashboard stats"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Error retrieving dashboard stats: " + e.getMessage()));
        }
    }
}
