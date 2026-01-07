package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.Review;
import za.ac.styling.domain.User;
import za.ac.styling.repository.ReviewRepository;
import za.ac.styling.service.ReviewService;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private ReviewRepository reviewRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review create(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public Review read(Integer id) {
        return reviewRepository.findById(id).orElse(null);
    }

    @Override
    public Review update(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getAll() {
        return reviewRepository.findAll();
    }

    @Override
    public List<Review> findByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    @Override
    public List<Review> findByProductId(Integer productId) {
        return reviewRepository.findByProductProductId(productId);
    }

    @Override
    public List<Review> findByUser(User user) {
        return reviewRepository.findByUser(user);
    }

    @Override
    public List<Review> findByUserId(Integer userId) {
        return reviewRepository.findByUserUserId(userId);
    }

    @Override
    public List<Review> findByUserIdAndProductId(Integer userId, Integer productId) {
        return reviewRepository.findByUserUserIdAndProductProductId(userId, productId);
    }

    @Override
    public List<Review> findByRating(int rating) {
        return reviewRepository.findByRating(rating);
    }

    @Override
    public List<Review> findRecentReviewsByProduct(Product product) {
        return reviewRepository.findByProductOrderByReviewDateDesc(product);
    }

    @Override
    public double calculateAverageRating(Product product) {
        List<Review> reviews = reviewRepository.findByProduct(product);
        if (reviews.isEmpty()) {
            return 0.0;
        }

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    @Override
    public long countByProduct(Product product) {
        return reviewRepository.countByProduct(product);
    }

    @Override
    public void delete(Integer id) {
        reviewRepository.deleteById(id);
    }
}
