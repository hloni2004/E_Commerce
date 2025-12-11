package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.*;

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
     * Delete cart items by product
     */
    @Transactional
    @Modifying
    void deleteByProduct(Product product);

    /**
     * Delete cart items by colour
     */
    @Transactional
    @Modifying
    void deleteByColour(ProductColour colour);

    /**
     * Delete cart items by size
     */
    @Transactional
    @Modifying
    void deleteBySize(ProductColourSize size);

    /**
     * Count items in a cart
     */
    long countByCart(Cart cart);
}
