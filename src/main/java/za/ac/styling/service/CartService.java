package za.ac.styling.service;

import za.ac.styling.domain.Cart;
import za.ac.styling.domain.User;

import java.util.Optional;

/**
 * Service interface for Cart entity
 */
public interface CartService extends IService<Cart, Integer> {

    /**
     * Find cart by user
     */
    Optional<Cart> findByUser(User user);

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(Integer userId);

    /**
     * Create cart for user
     */
    Cart createCartForUser(User user);

    /**
     * Clear cart
     */
    void clearCart(Integer cartId);

    /**
     * Get cart total
     */
    double getCartTotal(Integer cartId);
}
