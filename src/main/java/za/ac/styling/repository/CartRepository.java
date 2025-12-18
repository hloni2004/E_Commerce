package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Cart;
import za.ac.styling.domain.User;

import java.util.Optional;

/**
 * Repository interface for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    /**
     * Find cart by user
     */
    Optional<Cart> findByUser(User user);

    /**
     * Find cart by user ID with items eagerly fetched
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.userId = :userId")
    Optional<Cart> findByUserUserIdWithItems(@Param("userId") Integer userId);

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserUserId(Integer userId);

    /**
     * Check if user has a cart
     */
    boolean existsByUser(User user);
}
