package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.Review;
import za.ac.styling.repository.OrderRepository;
import za.ac.styling.service.ReviewService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private ReviewService reviewService;
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestBody Review review) {
        try {
            // Validate that user has purchased the product
            if (review.getUser() == null || review.getProduct() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "User and Product are required"));
            }
            
            boolean hasPurchased = orderRepository.hasUserPurchasedProduct(
                review.getUser().getUserId(), 
                review.getProduct().getProductId()
            );
            
            if (!hasPurchased) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "You can only review products you have purchased"));
            }
            
            Review created = reviewService.create(review);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error creating review: " + e.getMessage()));
        }
    }
    
    @GetMapping("/can-review/{userId}/{productId}")
    public ResponseEntity<?> canUserReview(@PathVariable Integer userId, @PathVariable Integer productId) {
        try {
            boolean hasPurchased = orderRepository.hasUserPurchasedProduct(userId, productId);
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "canReview", hasPurchased,
                "message", hasPurchased ? "You can review this product" : "You must purchase this product before reviewing"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error checking review eligibility: " + e.getMessage()));
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Integer id) {
        try {
            Review review = reviewService.read(id);
            if (review == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Review not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", review));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving review: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody Review review) {
        try {
            Review updated = reviewService.update(review);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Review not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error updating review: " + e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<Review> reviews = reviewService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving reviews: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer id) {
        try {
            reviewService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Review deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting review: " + e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProduct(@PathVariable Integer productId) {
        try {
            List<Review> reviews = reviewService.findByProductId(productId);
            return ResponseEntity.ok(Map.of("success", true, "data", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving product reviews: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReviewsByUser(@PathVariable Integer userId) {
        try {
            List<Review> reviews = reviewService.findByUserId(userId);
            return ResponseEntity.ok(Map.of("success", true, "data", reviews));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving user reviews: " + e.getMessage()));
        }
    }
}
