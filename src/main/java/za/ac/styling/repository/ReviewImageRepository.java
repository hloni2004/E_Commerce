package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Review;
import za.ac.styling.domain.ReviewImage;

import java.util.List;

/**
 * Repository interface for ReviewImage entity
 */
@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    /**
     * Find all images for a review
     */
    List<ReviewImage> findByReview(Review review);

    /**
     * Find all images for a review by review ID
     */
    List<ReviewImage> findByReviewReviewId(Long reviewId);
}
