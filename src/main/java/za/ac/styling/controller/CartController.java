package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.Cart;
import za.ac.styling.service.CartService;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private CartService cartService;

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/create")
    public ResponseEntity<Cart> createCart(@RequestBody Cart cart) {
        try {
            Cart created = cartService.create(cart);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Integer id) {
        try {
            Cart cart = cartService.read(id);
            if (cart == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Cart not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", cart));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving cart: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody Cart cart) {
        try {
            Cart updated = cartService.update(cart);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Cart not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error updating cart: " + e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<Cart> carts = cartService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", carts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving carts: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCart(@PathVariable Integer id) {
        try {
            cartService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cart deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting cart: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCartByUser(@PathVariable Integer userId) {
        try {
            Cart cart = cartService.findByUserId(userId)
                .orElse(null);
            if (cart == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Cart not found for user"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", cart));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving cart: " + e.getMessage()));
        }
    }

    @PostMapping("/add-item")
    public ResponseEntity<?> addItemToCart(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            Integer productId = (Integer) request.get("productId");
            Integer colourId = (Integer) request.get("colourId");
            Integer sizeId = (Integer) request.get("sizeId");
            Integer quantity = (Integer) request.get("quantity");

            // Get or create cart for user
            Cart cart = cartService.findByUserId(userId).orElse(null);
            if (cart == null) {
                // Create new cart for user
                cart = Cart.builder()
                        .user(za.ac.styling.domain.User.builder().userId(userId).build())
                        .createdAt(java.time.LocalDateTime.now())
                        .updatedAt(java.time.LocalDateTime.now())
                        .build();
                cart = cartService.create(cart);
            }

            // Create cart item
            za.ac.styling.domain.CartItem cartItem = za.ac.styling.domain.CartItem.builder()
                    .cart(cart)
                    .product(za.ac.styling.domain.Product.builder().productId(productId).build())
                    .colour(za.ac.styling.domain.ProductColour.builder().colourId(colourId).build())
                    .size(za.ac.styling.domain.ProductColourSize.builder().sizeId(sizeId).build())
                    .quantity(quantity)
                    .build();

            // Note: You'll need to add a CartItemRepository and save method
            // For now, we'll update the cart's items list
            if (cart.getItems() == null) {
                cart.setItems(new java.util.ArrayList<>());
            }
            cart.getItems().add(cartItem);
            cart.setUpdatedAt(java.time.LocalDateTime.now());
            cartService.update(cart);

            return ResponseEntity.ok(Map.of("success", true, "message", "Item added to cart"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error adding item to cart: " + e.getMessage()));
        }
    }
}
