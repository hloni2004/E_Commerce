package za.ac.styling.factory;

import za.ac.styling.domain.Cart;
import za.ac.styling.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Factory class responsible for creating Cart objects
 */
public class CartFactory {

    /**
     * Creates a new Cart for a user
     */
    public static Cart createCart(User user) {
        
        // Validate input data
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        return Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an empty cart without user association (for guest checkout)
     */
    public static Cart createGuestCart() {
        
        return Cart.builder()
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a cart and associates it with the user bidirectionally
     */
    public static Cart createCartWithUserAssociation(User user) {
        
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        Cart cart = createCart(user);
        user.setCart(cart);
        
        return cart;
    }
}
