package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    List<CartItem> findByCart(Cart cart);

    List<CartItem> findByCartCartId(Integer cartId);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    void deleteByCart(Cart cart);

    @Transactional
    @Modifying
    void deleteByProduct(Product product);

    @Transactional
    @Modifying
    void deleteByColour(ProductColour colour);

    @Transactional
    @Modifying
    void deleteBySize(ProductColourSize size);

    long countByCart(Cart cart);
}
