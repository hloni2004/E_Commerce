package za.ac.styling.service;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.Review;
import za.ac.styling.domain.User;

import java.util.List;

/**
 * Service interface for Review entity
 */
public interface ReviewService extends IService<Review, Integer> {

    /**
     * Find all reviews for a product
     */
    List<Review> findByProduct(Product product);

    /**
     * Find all reviews for a product by product ID
     */
    List<Review> findByProductId(Integer productId);

    /**
     * Find all reviews by a user
     */
    List<Review> findByUser(User user);

    /**
     * Find all reviews by a user ID
     */
    List<Review> findByUserId(Integer userId);
    
    /**
     * Find reviews by user and product
     */
    List<Review> findByUserIdAndProductId(Integer userId, Integer productId);

    /**
     * Find reviews by rating
     */
    List<Review> findByRating(int rating);

    /**
     * Find recent reviews for a product
     */
    List<Review> findRecentReviewsByProduct(Product product);

    /**
     * Calculate average rating for a product
     */
    double calculateAverageRating(Product product);

    /**
     * Count reviews for a product
     */
    long countByProduct(Product product);
}
