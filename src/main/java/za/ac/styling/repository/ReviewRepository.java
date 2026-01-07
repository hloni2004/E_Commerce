package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.Review;
import za.ac.styling.domain.User;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProduct(Product product);

    List<Review> findByProductProductId(Integer productId);

    List<Review> findByUser(User user);

    List<Review> findByUserUserId(Integer userId);

    List<Review> findByUserUserIdAndProductProductId(Integer userId, Integer productId);

    List<Review> findByRating(int rating);

    List<Review> findByProductAndRating(Product product, int rating);

    List<Review> findByProductOrderByReviewDateDesc(Product product);

    List<Review> findByProductAndRatingGreaterThanEqual(Product product, int minRating);

    long countByProduct(Product product);
}
