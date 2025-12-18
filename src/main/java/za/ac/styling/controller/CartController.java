package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.Cart;
import za.ac.styling.service.CartService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/carts", "/api/cart"})
public class CartController {

    private CartService cartService;
    
    @Autowired
    private za.ac.styling.repository.CartItemRepository cartItemRepository;
    
    @Autowired
    private za.ac.styling.repository.UserRepository userRepository;
    
    @Autowired
    private za.ac.styling.repository.ProductRepository productRepository;
    
    @Autowired
    private za.ac.styling.repository.ProductColourRepository productColourRepository;
    
    @Autowired
    private za.ac.styling.repository.ProductColourSizeRepository productColourSizeRepository;

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
            System.out.println("🛒 Fetching cart for user ID: " + userId);
            
            Cart cart = cartService.findByUserId(userId)
                .orElse(null);
                
            if (cart == null) {
                System.out.println("⚠️ No cart found for user " + userId + ", returning empty cart");
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "data", Map.of(
                        "cartId", 0,
                        "items", new ArrayList<>(),
                        "totalPrice", 0.0,
                        "totalItems", 0
                    )
                ));
            }
            
            System.out.println("✅ Cart found with " + cart.getItems().size() + " items");
            return ResponseEntity.ok(Map.of("success", true, "data", cart));
        } catch (Exception e) {
            System.err.println("❌ Error retrieving cart: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving cart: " + e.getMessage()));
        }
    }

    @PostMapping("/add-item")
    public ResponseEntity<?> addItemToCart(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("🛒 ADD TO CART Request: " + request);
            
            Integer userId = (Integer) request.get("userId");
            Integer productId = (Integer) request.get("productId");
            Integer colourId = (Integer) request.get("colourId");
            Integer sizeId = (Integer) request.get("sizeId");
            Integer quantity = (Integer) request.get("quantity");

            System.out.println("   User ID: " + userId + ", Product ID: " + productId + 
                             ", Colour ID: " + colourId + ", Size ID: " + sizeId + ", Qty: " + quantity);

            // Get user
            za.ac.styling.domain.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            System.out.println("✓ User found: " + user.getEmail());

            // Get or create cart for user
            Cart cart = cartService.findByUserId(userId).orElse(null);
            if (cart == null) {
                System.out.println("   Creating new cart for user " + userId);
                cart = Cart.builder()
                        .user(user)
                        .createdAt(java.time.LocalDateTime.now())
                        .updatedAt(java.time.LocalDateTime.now())
                        .build();
                cart = cartService.create(cart);
                System.out.println("✓ New cart created with ID: " + cart.getCartId());
            } else {
                System.out.println("✓ Existing cart found with ID: " + cart.getCartId());
            }

            // Load full entities
            za.ac.styling.domain.Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
            System.out.println("✓ Product found: " + product.getName());
            
            za.ac.styling.domain.ProductColour colour = productColourRepository.findById(colourId)
                .orElseThrow(() -> new RuntimeException("Colour not found"));
            System.out.println("✓ Colour found: " + colour.getName());
            
            za.ac.styling.domain.ProductColourSize size = productColourSizeRepository.findById(sizeId)
                .orElseThrow(() -> new RuntimeException("Size not found"));
            System.out.println("✓ Size found: " + size.getSizeName() + " (Stock: " + size.getStockQuantity() + ")");

            // Calculate available stock (total stock - reserved stock)
            int availableStock = size.getStockQuantity() - size.getReservedQuantity();
            System.out.println("   Available stock: " + availableStock + " (Total: " + size.getStockQuantity() + 
                             ", Reserved: " + size.getReservedQuantity() + ")");

            // Check if item already exists in cart
            za.ac.styling.domain.CartItem existingItem = cart.getItems() != null ? 
                cart.getItems().stream()
                    .filter(item -> item.getProduct().getProductId().equals(productId) 
                                 && item.getColour().getColourId().equals(colourId)
                                 && item.getSize().getSizeId().equals(sizeId))
                    .findFirst()
                    .orElse(null) : null;

            if (existingItem != null) {
                System.out.println("   Item already in cart, updating quantity from " + 
                                 existingItem.getQuantity() + " to " + (existingItem.getQuantity() + quantity));
                
                // Check if new total quantity exceeds available stock
                int newQuantity = existingItem.getQuantity() + quantity;
                if (newQuantity > availableStock) {
                    System.out.println("❌ Insufficient stock for update");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                            "success", false, 
                            "message", "Insufficient stock. Only " + availableStock + " items available.",
                            "availableStock", availableStock
                        ));
                }
                // Update quantity
                existingItem.setQuantity(newQuantity);
                cartItemRepository.save(existingItem);
                System.out.println("✅ Cart item updated successfully");
            } else {
                System.out.println("   Adding new item to cart");
                
                // Check if quantity exceeds available stock
                if (quantity > availableStock) {
                    System.out.println("❌ Insufficient stock for new item");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                            "success", false, 
                            "message", "Insufficient stock. Only " + availableStock + " items available.",
                            "availableStock", availableStock
                        ));
                }
                
                // Create new cart item
                za.ac.styling.domain.CartItem cartItem = za.ac.styling.domain.CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .colour(colour)
                        .size(size)
                        .quantity(quantity)
                        .build();
                
                za.ac.styling.domain.CartItem savedItem = cartItemRepository.save(cartItem);
                System.out.println("✅ New cart item created with ID: " + savedItem.getCartItemId());
            }

            cart.setUpdatedAt(java.time.LocalDateTime.now());
            Cart updatedCart = cartService.update(cart);
            
            System.out.println("✅ Cart updated. Total items in cart: " + 
                             (updatedCart.getItems() != null ? updatedCart.getItems().size() : 0));

            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Item added to cart",
                "cartId", cart.getCartId(),
                "itemCount", updatedCart.getItems() != null ? updatedCart.getItems().size() : 0
            ));
        } catch (Exception e) {
            System.err.println("❌ ERROR adding to cart: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error adding item to cart: " + e.getMessage()));
        }
    }

    @PostMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Integer userId) {
        try {
            // Get cart for user
            Cart cart = cartService.findByUserId(userId).orElse(null);
            if (cart == null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "No cart found for user"));
            }

            // Delete the entire cart (cascade will delete all cart items due to orphanRemoval = true)
            cartService.delete(cart.getCartId());

            return ResponseEntity.ok(Map.of("success", true, "message", "Cart and cart items deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error clearing cart: " + e.getMessage()));
        }
    }
}
