package za.ac.styling.service;

import za.ac.styling.domain.Cart;
import za.ac.styling.domain.CartItem;
import za.ac.styling.domain.Product;

import java.util.List;

/**
 * Service interface for CartItem entity
 */
public interface CartItemService extends IService<CartItem, Integer> {

    /**
     * Find all items in a cart
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Find all items in a cart by cart ID
     */
    List<CartItem> findByCartId(Integer cartId);

    /**
     * Add item to cart
     */
    CartItem addToCart(Cart cart, Product product, Integer quantity);

    /**
     * Update item quantity
     */
    CartItem updateQuantity(Integer cartItemId, Integer quantity);

    /**
     * Remove item from cart
     */
    void removeFromCart(Integer cartItemId);

    /**
     * Count items in cart
     */
    long countByCart(Cart cart);
}
