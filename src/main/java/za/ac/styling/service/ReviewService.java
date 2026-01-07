package za.ac.styling.service;

import za.ac.styling.domain.Product;
import za.ac.styling.domain.Review;
import za.ac.styling.domain.User;

import java.util.List;

public interface ReviewService extends IService<Review, Integer> {

    List<Review> findByProduct(Product product);

    List<Review> findByProductId(Integer productId);

    List<Review> findByUser(User user);

    List<Review> findByUserId(Integer userId);

    List<Review> findByUserIdAndProductId(Integer userId, Integer productId);

    List<Review> findByRating(int rating);

    List<Review> findRecentReviewsByProduct(Product product);

    double calculateAverageRating(Product product);

    long countByProduct(Product product);
}
