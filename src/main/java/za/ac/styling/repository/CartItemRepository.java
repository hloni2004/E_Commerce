package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Cart;
import za.ac.styling.domain.CartItem;
import za.ac.styling.domain.Product;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    /**
     * Find all items in a cart
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Find all items in a cart by cart ID
     */
    List<CartItem> findByCartCartId(Integer cartId);

    /**
     * Find cart item by cart and product
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * Delete all items in a cart
     */
    void deleteByCart(Cart cart);

    /**
     * Count items in a cart
     */
    long countByCart(Cart cart);
}
