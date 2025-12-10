package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.Review;
import za.ac.styling.domain.User;

import java.util.List;

/**
 * Repository interface for Review entity
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    /**
     * Find all reviews for a product
     */
    List<Review> findByProduct(Product product);

    /**
     * Find all reviews for a product by product ID
     */
    List<Review> findByProductProductId(Integer productId);

    /**
     * Find all reviews by a user
     */
    List<Review> findByUser(User user);

    /**
     * Find all reviews by a user by user ID
     */
    List<Review> findByUserUserId(Integer userId);

    /**
     * Find reviews by rating
     */
    List<Review> findByRating(int rating);

    /**
     * Find reviews by product and rating
     */
    List<Review> findByProductAndRating(Product product, int rating);

    /**
     * Find reviews ordered by date (most recent first)
     */
    List<Review> findByProductOrderByReviewDateDesc(Product product);

    /**
     * Find reviews by rating range for a product
     */
    List<Review> findByProductAndRatingGreaterThanEqual(Product product, int minRating);

    /**
     * Count reviews for a product
     */
    long countByProduct(Product product);
}
