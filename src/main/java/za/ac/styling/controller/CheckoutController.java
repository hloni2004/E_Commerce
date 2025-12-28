package za.ac.styling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.*;
import za.ac.styling.repository.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartRepository cartRepository;
    private final za.ac.styling.service.CartService cartService;
    private final OrderRepository orderRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final za.ac.styling.service.EmailService emailService;
    private final za.ac.styling.service.InventoryService inventoryService;

    @GetMapping("/shipping-methods")
    public ResponseEntity<?> getActiveShippingMethods() {
        List<ShippingMethod> methods = shippingMethodRepository.findByIsActiveTrueOrderByCostAsc();
        return ResponseEntity.ok(methods);
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            Long shippingMethodId = Long.valueOf(request.get("shippingMethodId").toString());
            Long shippingAddressId = Long.valueOf(request.get("shippingAddressId").toString());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Use service method that performs fetch join to ensure items are eagerly
            // loaded
            Cart cart = cartService.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
            }

            // Log cart details for diagnostics to help track quantity/price mismatches
            System.out.println("🛒 Creating order for user " + userId + ", cartId=" + cart.getCartId() + ", items=");
            cart.getItems().forEach(ci -> {
                System.out.println(
                        "   - productId=" + (ci.getProduct() != null ? ci.getProduct().getProductId() : "<null>") +
                                ", qty=" + ci.getQuantity() + ", unitPrice="
                                + (ci.getProduct() != null ? ci.getProduct().getBasePrice() : "<null>"));
            });

            ShippingMethod shippingMethod = shippingMethodRepository.findById(shippingMethodId)
                    .orElseThrow(() -> new RuntimeException("Shipping method not found"));

            Address shippingAddress = addressRepository.findById(shippingAddressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            // Calculate totals
            double subtotal = cart.getItems().stream()
                    .mapToDouble(item -> item.getQuantity() * item.getProduct().getBasePrice())
                    .sum();

            double shippingCost = shippingMethod.getCost();
            double taxAmount = subtotal * 0.15; // 15% VAT
            double totalAmount = subtotal + shippingCost + taxAmount;

            // Generate order number
            String orderNumber = "ORD-" + System.currentTimeMillis();

            // Create order
            Order order = Order.builder()
                    .user(user)
                    .orderNumber(orderNumber)
                    .subtotal(subtotal)
                    .shippingCost(shippingCost)
                    .taxAmount(taxAmount)
                    .totalAmount(totalAmount)
                    .orderDate(new Date())
                    .shippingMethod(shippingMethod)
                    .shippingAddress(shippingAddress)
                    .status(OrderStatus.PENDING)
                    .build();

            // Create order items from cart items
            List<OrderItem> orderItems = cart.getItems().stream()
                    .map(cartItem -> {
                        double itemPrice = cartItem.getProduct().getBasePrice();
                        int itemQuantity = cartItem.getQuantity();
                        double itemTotal = itemQuantity * itemPrice;
                        return OrderItem.builder()
                                .order(order)
                                .product(cartItem.getProduct())
                                .colour(cartItem.getColour())
                                .colourSize(cartItem.getSize())
                                .quantity(itemQuantity)
                                .price(itemPrice)
                                .subtotal(itemTotal)
                                .totalPrice(itemTotal)
                                .build();
                    })
                    .toList();

            order.setItems(orderItems);

            // Check stock availability before creating order
            if (!inventoryService.checkStockAvailability(orderItems)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Some items in your cart are out of stock or have insufficient quantity",
                        "errorType", "INSUFFICIENT_STOCK"));
            }

            // Reserve stock for the order
            try {
                inventoryService.reserveStock(orderItems);
            } catch (za.ac.styling.service.InventoryService.InsufficientStockException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", e.getMessage(),
                        "errorType", "INSUFFICIENT_STOCK"));
            }

            // Save order
            Order savedOrder = orderRepository.save(order);

            // Commit stock (convert reserved to sold)
            inventoryService.commitStock(orderItems);

            // Send order confirmation email asynchronously (loosely coupled)
            // This ensures the order is saved even if email fails
            final Order finalOrder = savedOrder;
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    // Ensure all relationships are loaded before sending email
                    finalOrder.getItems().size(); // Force load items
                    finalOrder.getItems().forEach(item -> {
                        item.getProduct().getName(); // Force load product
                        item.getColour().getName(); // Force load colour
                        item.getColourSize().getSizeName(); // Force load size
                    });

                    // Log recipient email and order number for diagnostics
                    String recipient = finalOrder.getUser() != null ? finalOrder.getUser().getEmail() : "<unknown>";
                    System.out.println("Attempting to send order confirmation email to " + recipient + " for order "
                            + finalOrder.getOrderNumber());

                    emailService.sendOrderConfirmationEmail(finalOrder);
                } catch (Exception e) {
                    System.err.println("Failed to send confirmation email asynchronously: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Delete cart items and cart after successful order (defensive)
            try {
                if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                    cart.getItems().clear();
                    cartService.update(cart); // Persist removal of items
                    System.out.println("✅ Cleared cart items for cartId=" + cart.getCartId());
                }
                cartRepository.delete(cart);
                System.out.println("✅ Deleted cart for userId=" + userId + ", cartId=" + cart.getCartId());
            } catch (Exception ex) {
                System.err.println("❌ Failed to fully delete cart or items: " + ex.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", savedOrder.getOrderId());
            response.put("orderNumber", savedOrder.getOrderNumber());
            response.put("totalAmount", savedOrder.getTotalAmount());
            response.put("status", savedOrder.getStatus());
            response.put("message", "Order placed successfully! Check your email for confirmation.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("=== ORDER CREATION ERROR ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            System.err.println("============================");

            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred",
                    "errorType", e.getClass().getSimpleName()));
        }
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> createAddress(@RequestBody Address address) {
        try {
            User user = userRepository.findById(address.getUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            address.setUser(user);

            // If this is set as default, unset other defaults
            if (address.isDefault()) {
                List<Address> userAddresses = addressRepository.findByUserUserId(user.getUserId());
                userAddresses.forEach(addr -> {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                });
            }

            Address savedAddress = addressRepository.save(address);
            return ResponseEntity.ok(savedAddress);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/addresses/user/{userId}")
    public ResponseEntity<?> getUserAddresses(@PathVariable Integer userId) {
        List<Address> addresses = addressRepository.findByUserUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable Long addressId, @RequestBody Address address) {
        try {
            Address existing = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            existing.setFullName(address.getFullName());
            existing.setPhone(address.getPhone());
            existing.setAddressLine1(address.getAddressLine1());
            existing.setAddressLine2(address.getAddressLine2());
            existing.setCity(address.getCity());
            existing.setProvince(address.getProvince());
            existing.setPostalCode(address.getPostalCode());
            existing.setCountry(address.getCountry());

            // If setting as default, unset other defaults
            if (address.isDefault() && !existing.isDefault()) {
                List<Address> userAddresses = addressRepository.findByUserUserId(existing.getUser().getUserId());
                userAddresses.forEach(addr -> {
                    if (!addr.getAddressId().equals(addressId)) {
                        addr.setDefault(false);
                        addressRepository.save(addr);
                    }
                });
            }
            existing.setDefault(address.isDefault());

            Address updated = addressRepository.save(existing);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        try {
            addressRepository.deleteById(addressId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Address deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/cart/{userId}")
    public ResponseEntity<?> getCartWithItems(@PathVariable Integer userId) {
        try {
            System.out.println("🛒 Checkout: Fetching cart for user ID: " + userId);

            Cart cart = cartService.findByUserId(userId).orElse(null);
            if (cart == null) {
                System.out.println("⚠️ No cart found for user " + userId);
                return ResponseEntity.badRequest().body(Map.of("error", "Cart not found"));
            }

            // Build response with correct image for each cart item
            var cartItems = cart.getItems();
            var itemsWithImages = new java.util.ArrayList<>();
            for (var ci : cartItems) {
                String imageUrl = null;
                // Try to get image for selected colour
                if (ci.getColour() != null && ci.getColour().getProduct() != null
                        && ci.getColour().getProduct().getImages() != null) {
                    var images = ci.getColour().getProduct().getImages();
                    var match = images.stream()
                            .filter(img -> img.getAltText() != null
                                    && img.getAltText().toLowerCase().contains(ci.getColour().getName().toLowerCase()))
                            .findFirst();
                    if (match.isPresent()) {
                        imageUrl = match.get().getImageUrl();
                    }
                }
                // Fallback to product primary image
                if (imageUrl == null && ci.getProduct() != null && ci.getProduct().getPrimaryImage() != null) {
                    imageUrl = ci.getProduct().getPrimaryImage().getImageUrl();
                }
                // Fallback to any image
                if (imageUrl == null && ci.getProduct() != null && ci.getProduct().getImages() != null
                        && !ci.getProduct().getImages().isEmpty()) {
                    imageUrl = ci.getProduct().getImages().get(0).getImageUrl();
                }
                var itemMap = new java.util.HashMap<String, Object>();
                itemMap.put("cartItemId", ci.getCartItemId());
                itemMap.put("product", Map.of(
                        "productId", ci.getProduct() != null ? ci.getProduct().getProductId() : null,
                        "name", ci.getProduct() != null ? ci.getProduct().getName() : null,
                        "basePrice", ci.getProduct() != null ? ci.getProduct().getBasePrice() : null,
                        "imageUrl", imageUrl));
                itemMap.put("colour", Map.of(
                        "colourId", ci.getColour() != null ? ci.getColour().getColourId() : null,
                        "name", ci.getColour() != null ? ci.getColour().getName() : null));
                itemMap.put("size", Map.of(
                        "sizeId", ci.getSize() != null ? ci.getSize().getSizeId() : null,
                        "sizeName", ci.getSize() != null ? ci.getSize().getSizeName() : null));
                itemMap.put("quantity", ci.getQuantity());
                itemsWithImages.add(itemMap);
            }
            var response = Map.of(
                    "cartId", cart.getCartId(),
                    "items", itemsWithImages);
            System.out
                    .println("✅ Cart found with " + (cart.getItems() != null ? cart.getItems().size() : 0) + " items");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error fetching cart: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
