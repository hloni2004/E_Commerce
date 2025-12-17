package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import za.ac.styling.domain.Review;
import za.ac.styling.domain.ReviewImage;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.User;
import za.ac.styling.repository.OrderRepository;
import za.ac.styling.repository.ProductRepository;
import za.ac.styling.repository.UserRepository;
import za.ac.styling.service.ReviewService;
import za.ac.styling.service.SupabaseStorageService;

import java.util.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private ReviewService reviewService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReview(
            @RequestParam("userId") Integer userId,
            @RequestParam("productId") Integer productId,
            @RequestParam("rating") int rating,
            @RequestParam("comment") String comment,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            // Validate rating
            if (rating < 1 || rating > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Rating must be between 1 and 5"));
            }
            
            // Fetch user and product
            User user = userRepository.findById(userId).orElse(null);
            Product product = productRepository.findById(productId).orElse(null);
            
            if (user == null || product == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Invalid user or product"));
            }
            
            // Validate that user has purchased the product
            boolean hasPurchased = orderRepository.hasUserPurchasedProduct(userId, productId);
            
            if (!hasPurchased) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "You can only review products you have purchased"));
            }
            
            // Check if user already reviewed this product
            List<Review> existingReviews = reviewService.findByUserIdAndProductId(userId, productId);
            if (!existingReviews.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "You have already reviewed this product"));
            }
            
            // Create review
            Review review = Review.builder()
                .user(user)
                .product(product)
                .productId(productId)
                .rating(rating)
                .comment(comment)
                .reviewDate(new Date())
                .verified(true)
                .helpfulCount(0)
                .build();
            
            // Save review first to get the ID
            Review created = reviewService.create(review);
            
            // Add images if provided (upload to Supabase)
            if (images != null && images.length > 0) {
                List<ReviewImage> reviewImages = new ArrayList<>();
                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        try {
                            // Upload to Supabase
                            Integer reviewIdInt = created.getReviewId();
                            Long reviewId = reviewIdInt != null ? reviewIdInt.longValue() : 0L;
                            SupabaseStorageService.UploadResult uploadResult = 
                                supabaseStorageService.uploadReviewImage(file, reviewId);
                            
                            ReviewImage reviewImage = ReviewImage.builder()
                                .review(created)
                                .supabaseUrl(uploadResult.getUrl())
                                .bucketPath(uploadResult.getPath())
                                .contentType(file.getContentType())
                                .build();
                            reviewImages.add(reviewImage);
                        } catch (Exception e) {
                            System.err.println("Failed to upload review image: " + e.getMessage());
                            // Continue with other images even if one fails
                        }
                    }
                }
                created.setImages(reviewImages);
                // Update review with images
                created = reviewService.update(created);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", created, "message", "Review submitted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
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
