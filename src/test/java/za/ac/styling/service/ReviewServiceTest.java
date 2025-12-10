package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.Review;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;
import za.ac.styling.factory.CategoryFactory;
import za.ac.styling.factory.ProductFactory;
import za.ac.styling.factory.ReviewFactory;
import za.ac.styling.factory.RoleFactory;
import za.ac.styling.factory.UserFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RoleService roleService;

    private User testUser;
    private Product testProduct;
    private Review testReview;

    @BeforeEach
    void setUp() {
        Role role = RoleFactory.createRole("CUSTOMER");
        role = roleService.create(role);

        testUser = UserFactory.createUser(
                "reviewuser",
                "reviewer@example.com",
                "password123",
                "Reviewer",
                "User",
                "3334445555",
                role
        );
        testUser = userService.create(testUser);

        Category category = CategoryFactory.createCategory("Electronics", "Devices");
        category = categoryService.create(category);

        testProduct = ProductFactory.createProduct(
                "Laptop",
                "Gaming Laptop",
                1200.00,
                "SKU-LAPTOP-REVIEW",
                category
        );
        testProduct = productService.create(testProduct);

        testReview = ReviewFactory.createReview(testUser, testProduct, 5, "Excellent product!");
    }

    @Test
    void testCreate() {
        Review created = reviewService.create(testReview);
        assertNotNull(created);
        assertNotNull(created.getReviewId());
        assertEquals(5, created.getRating());
    }

    @Test
    void testRead() {
        Review created = reviewService.create(testReview);
        Review found = reviewService.read(created.getReviewId());
        assertNotNull(found);
        assertEquals(created.getReviewId(), found.getReviewId());
    }

    @Test
    void testUpdate() {
        Review created = reviewService.create(testReview);
        created.setRating(4);
        Review updated = reviewService.update(created);
        assertNotNull(updated);
        assertEquals(4, updated.getRating());
    }

    @Test
    void testGetAll() {
        reviewService.create(testReview);
        List<Review> reviews = reviewService.getAll();
        assertNotNull(reviews);
        assertFalse(reviews.isEmpty());
    }

    @Test
    void testFindByProduct() {
        reviewService.create(testReview);
        List<Review> reviews = reviewService.findByProduct(testProduct);
        assertNotNull(reviews);
        assertFalse(reviews.isEmpty());
    }

    @Test
    void testFindByUser() {
        reviewService.create(testReview);
        List<Review> reviews = reviewService.findByUser(testUser);
        assertNotNull(reviews);
        assertFalse(reviews.isEmpty());
    }

    @Test
    void testFindByRating() {
        reviewService.create(testReview);
        List<Review> reviews = reviewService.findByRating(5);
        assertNotNull(reviews);
        assertFalse(reviews.isEmpty());
    }

    @Test
    void testCalculateAverageRating() {
        reviewService.create(testReview);
        Review review2 = ReviewFactory.createReview(testUser, testProduct, 4, "Good product");
        reviewService.create(review2);

        double avgRating = reviewService.calculateAverageRating(testProduct);
        assertEquals(4.5, avgRating, 0.01);
    }

    @Test
    void testCountByProduct() {
        reviewService.create(testReview);
        long count = reviewService.countByProduct(testProduct);
        assertTrue(count >= 1);
    }
}
