package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.Review;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;

import static org.junit.jupiter.api.Assertions.*;

class ReviewFactoryTest {

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        Role customerRole = Role.builder()
                .roleId(1)
                .roleName("CUSTOMER")
                .build();

        testUser = User.builder()
                .userId(1)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(customerRole)
                .build();

        Category category = Category.builder()
                .categoryId(1L)
                .name("Electronics")
                .build();

        testProduct = Product.builder()
                .productId(1)
                .name("Test Product")
                .basePrice(99.99)
                .category(category)
                .build();
    }

    @Test
    void createReview_WithValidData_ShouldCreateReview() {
        // Arrange
        int rating = 5;
        String comment = "Excellent product!";

        // Act
        Review review = ReviewFactory.createReview(testUser, testProduct, rating, comment);

        // Assert
        assertNotNull(review);
        assertEquals(testUser, review.getUser());
        assertEquals(testProduct, review.getProduct());
        assertEquals(rating, review.getRating());
        assertEquals(comment, review.getComment());
        assertNotNull(review.getReviewDate());
    }

    @Test
    void createReview_WithNullUser_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReviewFactory.createReview(null, testProduct, 5, "Comment")
        );

        assertEquals("User is required", exception.getMessage());
    }

    @Test
    void createReview_WithNullProduct_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReviewFactory.createReview(testUser, null, 5, "Comment")
        );

        assertEquals("Product is required", exception.getMessage());
    }

    @Test
    void createReview_WithInvalidRatingTooLow_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReviewFactory.createReview(testUser, testProduct, 0, "Comment")
        );

        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void createReview_WithInvalidRatingTooHigh_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReviewFactory.createReview(testUser, testProduct, 6, "Comment")
        );

        assertEquals("Rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    void createRatingOnlyReview_ShouldCreateReviewWithoutComment() {
        // Arrange
        int rating = 4;

        // Act
        Review review = ReviewFactory.createRatingOnlyReview(testUser, testProduct, rating);

        // Assert
        assertNotNull(review);
        assertEquals(rating, review.getRating());
        assertNull(review.getComment());
    }

    @Test
    void createDetailedReview_WithValidComment_ShouldCreateReview() {
        // Arrange
        String longComment = "This is a detailed review with more than ten characters.";

        // Act
        Review review = ReviewFactory.createDetailedReview(testUser, testProduct, 5, longComment);

        // Assert
        assertNotNull(review);
        assertEquals(longComment, review.getComment());
    }

    @Test
    void createDetailedReview_WithEmptyComment_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReviewFactory.createDetailedReview(testUser, testProduct, 5, "")
        );

        assertEquals("Comment is required for detailed review", exception.getMessage());
    }

    @Test
    void createDetailedReview_WithShortComment_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReviewFactory.createDetailedReview(testUser, testProduct, 5, "Short")
        );

        assertEquals("Comment must be between 10 and 1000 characters", exception.getMessage());
    }

    @Test
    void createPositiveReview_WithRating4_ShouldCreateReview() {
        // Act
        Review review = ReviewFactory.createPositiveReview(testUser, testProduct, 4, "Good");

        // Assert
        assertNotNull(review);
        assertEquals(4, review.getRating());
    }

    @Test
    void createPositiveReview_WithRating5_ShouldCreateReview() {
        // Act
        Review review = ReviewFactory.createPositiveReview(testUser, testProduct, 5, "Excellent");

        // Assert
        assertNotNull(review);
        assertEquals(5, review.getRating());
    }

    @Test
    void createPositiveReview_WithRating3_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReviewFactory.createPositiveReview(testUser, testProduct, 3, "Average")
        );

        assertEquals("Positive review must have rating of 4 or 5", exception.getMessage());
    }

    @Test
    void createNegativeReview_WithRating1_ShouldCreateReview() {
        // Act
        Review review = ReviewFactory.createNegativeReview(testUser, testProduct, 1, "Poor quality");

        // Assert
        assertNotNull(review);
        assertEquals(1, review.getRating());
    }

    @Test
    void createNegativeReview_WithRating2_ShouldCreateReview() {
        // Act
        Review review = ReviewFactory.createNegativeReview(testUser, testProduct, 2, "Not good");

        // Assert
        assertNotNull(review);
        assertEquals(2, review.getRating());
    }

    @Test
    void createNegativeReview_WithRating3_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReviewFactory.createNegativeReview(testUser, testProduct, 3, "Average")
        );

        assertEquals("Negative review must have rating of 1 or 2", exception.getMessage());
    }

    @Test
    void createNegativeReview_WithEmptyComment_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReviewFactory.createNegativeReview(testUser, testProduct, 1, "")
        );

        assertEquals("Comment is required for negative review", exception.getMessage());
    }
}
