package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.Order;
import za.ac.styling.service.OrderService;
import za.ac.styling.service.EmailService;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private OrderService orderService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private za.ac.styling.service.InventoryService inventoryService;

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        try {
            Order created = orderService.create(order);

            // Check for low stock items and send alerts
            if (created.getItems() != null) {
                for (var item : created.getItems()) {
                    int currentStock = inventoryService.getAvailableStock(item.getColourSize().getSizeId());
                    int reorderLevel = item.getColourSize().getReorderLevel();

                    if (currentStock <= reorderLevel) {
                        emailService.sendLowStockAlert(
                                item.getProduct(),
                                item.getColourSize(),
                                currentStock,
                                reorderLevel);
                    }
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Integer id) {
        try {
            Order order = orderService.read(id);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Order not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving order: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody Order order) {
        try {
            Order updated = orderService.update(order);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Order not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error updating order: " + e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<Order> orders = orderService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving orders: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Integer id) {
        try {
            orderService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Order deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error deleting order: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Integer userId) {
        try {
            List<Order> orders = orderService.findByUserId(userId);

            // Force load lazy relationships to avoid serialization issues
            orders.forEach(order -> {
                if (order.getItems() != null) {
                    order.getItems().size(); // Force load items
                    order.getItems().forEach(item -> {
                        if (item.getProduct() != null) {
                            item.getProduct().getName(); // Force load product
                            if (item.getProduct().getPrimaryImage() != null) {
                                item.getProduct().getPrimaryImage().getImageId(); // Force load image
                            }
                        }
                        if (item.getColour() != null) {
                            item.getColour().getName(); // Force load colour
                        }
                        if (item.getColourSize() != null) {
                            item.getColourSize().getSizeName(); // Force load size
                        }
                    });
                }
                if (order.getShippingMethod() != null) {
                    order.getShippingMethod().getName(); // Force load shipping method
                }
                if (order.getShippingAddress() != null) {
                    order.getShippingAddress().getFullName(); // Force load address
                }
            });

            return ResponseEntity.ok(Map.of("success", true, "data", orders));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving orders: " + e.getMessage()));
        }
    }

    @PostMapping("/reorder/{orderId}")
    public ResponseEntity<?> reorderItems(@PathVariable Integer orderId) {
        try {
            Order originalOrder = orderService.read(orderId);
            if (originalOrder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Order not found"));
            }

            // Return order items for reordering - frontend will add them to cart
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", originalOrder.getItems(),
                    "message", "Items ready to add to cart"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error reordering: " + e.getMessage()));
        }
    }

    /**
     * Update order status in real-time
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestBody Map<String, String> request) {
        try {
            String newStatus = request.get("status");
            Order order = orderService.read(orderId);

            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Order not found"));
            }

            za.ac.styling.domain.OrderStatus oldStatus = order.getStatus();
            za.ac.styling.domain.OrderStatus status = za.ac.styling.domain.OrderStatus.valueOf(newStatus);
            order.setStatus(status);

            // Handle inventory for cancelled orders
            if (status == za.ac.styling.domain.OrderStatus.CANCELLED &&
                    (oldStatus == za.ac.styling.domain.OrderStatus.PENDING ||
                            oldStatus == za.ac.styling.domain.OrderStatus.PROCESSING)) {
                // Release reserved stock when order is cancelled
                inventoryService.releaseStock(order.getItems());
            }

            Order updated = orderService.update(order);

            // Send email notification to customer about status change
            emailService.sendOrderStatusChangeEmail(updated, oldStatus, status);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Order status updated to " + status + " and customer notified"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid order status"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error updating order status: " + e.getMessage()));
        }
    }

    /**
     * Cancel order and restore inventory
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId) {
        try {
            Order order = orderService.read(orderId);

            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Order not found"));
            }

            if (order.getStatus() == za.ac.styling.domain.OrderStatus.DELIVERED ||
                    order.getStatus() == za.ac.styling.domain.OrderStatus.CANCELLED) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Cannot cancel " + order.getStatus() + " orders"));
            }

            // Update status to cancelled
            order.setStatus(za.ac.styling.domain.OrderStatus.CANCELLED);

            // Release stock back to inventory
            inventoryService.releaseStock(order.getItems());

            Order updated = orderService.update(order);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Order cancelled and inventory restored in real-time"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error cancelling order: " + e.getMessage()));
        }
    }

    /**
     * Process return and restore inventory
     */
    @PostMapping("/{orderId}/return")
    public ResponseEntity<?> returnOrder(@PathVariable Integer orderId) {
        try {
            Order order = orderService.read(orderId);

            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Order not found"));
            }

            if (order.getStatus() != za.ac.styling.domain.OrderStatus.DELIVERED) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Only delivered orders can be returned"));
            }

            // Update status to returned
            order.setStatus(za.ac.styling.domain.OrderStatus.RETURNED);

            // Return stock to inventory
            inventoryService.returnStock(order.getItems());

            Order updated = orderService.update(order);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Order returned and inventory updated in real-time"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error processing return: " + e.getMessage()));
        }
    }

    /**
     * Get real-time inventory status for order items
     */
    @GetMapping("/{orderId}/inventory-status")
    public ResponseEntity<?> getOrderInventoryStatus(@PathVariable Integer orderId) {
        try {
            Order order = orderService.read(orderId);

            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Order not found"));
            }

            List<Map<String, Object>> inventoryStatus = order.getItems().stream()
                    .map(item -> {
                        int availableStock = inventoryService.getAvailableStock(item.getColourSize().getSizeId());
                        Map<String, Object> itemStatus = new java.util.HashMap<>();
                        itemStatus.put("productName", item.getProduct().getName());
                        itemStatus.put("size", item.getColourSize().getSizeName());
                        itemStatus.put("orderedQuantity", item.getQuantity());
                        itemStatus.put("currentAvailableStock", availableStock);
                        itemStatus.put("inStock", availableStock > 0);
                        return itemStatus;
                    })
                    .toList();

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("data", inventoryStatus);
            result.put("message", "Real-time inventory status retrieved");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error getting inventory status: " + e.getMessage()));
        }
    }
}
