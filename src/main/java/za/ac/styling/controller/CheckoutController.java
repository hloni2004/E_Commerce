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
@CrossOrigin(origins = "*")
public class CheckoutController {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

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
            
            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
            
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
            }
            
            ShippingMethod shippingMethod = shippingMethodRepository.findById(shippingMethodId)
                    .orElseThrow(() -> new RuntimeException("Shipping method not found"));
            
            Address shippingAddress = addressRepository.findById(shippingAddressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            
            // Calculate totals
            double subtotal = cart.getItems().stream()
                    .mapToDouble(item -> item.getQuantity() * item.getColourSize().getPrice())
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
                    .map(cartItem -> OrderItem.builder()
                            .order(order)
                            .product(cartItem.getProduct())
                            .colour(cartItem.getColour())
                            .colourSize(cartItem.getColourSize())
                            .quantity(cartItem.getQuantity())
                            .price(cartItem.getColourSize().getPrice())
                            .subtotal(cartItem.getQuantity() * cartItem.getColourSize().getPrice())
                            .build())
                    .toList();
            
            order.setItems(orderItems);
            
            // Save order
            Order savedOrder = orderRepository.save(order);
            
            // Clear cart
            cart.getItems().clear();
            cartRepository.save(cart);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", savedOrder.getOrderId());
            response.put("orderNumber", savedOrder.getOrderNumber());
            response.put("totalAmount", savedOrder.getTotalAmount());
            response.put("status", savedOrder.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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

    @GetMapping("/cart/{userId}")
    public ResponseEntity<?> getCartWithItems(@PathVariable Integer userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
            
            return ResponseEntity.ok(cart);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
