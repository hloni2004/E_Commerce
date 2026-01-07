package za.ac.styling.factory;

import za.ac.styling.domain.Review;
import za.ac.styling.domain.User;
import za.ac.styling.domain.Product;
import za.ac.styling.util.ValidationHelper;

import java.util.Date;

public class ReviewFactory {

    public static Review createReview(User user, Product product, int rating, String comment) {

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }

        if (!ValidationHelper.isValidRating(rating)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        return Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .comment(comment)
                .reviewDate(new Date())
                .build();
    }

    public static Review createRatingOnlyReview(User user, Product product, int rating) {

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }

        if (!ValidationHelper.isValidRating(rating)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        return Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .reviewDate(new Date())
                .build();
    }

    public static Review createDetailedReview(User user, Product product, int rating,
            String comment) {

        if (ValidationHelper.isNullOrEmpty(comment)) {
            throw new IllegalArgumentException("Comment is required for detailed review");
        }

        if (!ValidationHelper.isValidLength(comment, 10, 1000)) {
            throw new IllegalArgumentException("Comment must be between 10 and 1000 characters");
        }

        return createReview(user, product, rating, comment);
    }

    public static Review createPositiveReview(User user, Product product, int rating,
            String comment) {

        if (rating < 4 || rating > 5) {
            throw new IllegalArgumentException("Positive review must have rating of 4 or 5");
        }

        return createReview(user, product, rating, comment);
    }

    public static Review createNegativeReview(User user, Product product, int rating,
            String comment) {

        if (rating < 1 || rating > 2) {
            throw new IllegalArgumentException("Negative review must have rating of 1 or 2");
        }

        if (ValidationHelper.isNullOrEmpty(comment)) {
            throw new IllegalArgumentException("Comment is required for negative review");
        }

        return createReview(user, product, rating, comment);
    }
}
