package za.ac.styling.factory;

import za.ac.styling.domain.User;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.Cart;
import za.ac.styling.util.ValidationHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Factory class responsible for creating User objects
 */
public class UserFactory {



    /**
     * Creates a new User with complete information including phone and role
     */
    public static User createUser(String username, String email, String password,
                                  String firstName, String lastName, String phone, Role role) {

        // Validate input data
        if (!ValidationHelper.isValidUsername(username)) {
            throw new IllegalArgumentException("Invalid username");
        }

        if (!ValidationHelper.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!ValidationHelper.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (!ValidationHelper.isValidName(firstName) || !ValidationHelper.isValidName(lastName)) {
            throw new IllegalArgumentException("Invalid name format");
        }

        if (phone != null && !ValidationHelper.isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        return User.builder()
                .username(username)
                .email(email.toLowerCase())
                .password(password) // Should be hashed in production
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .role(role)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .addresses(new ArrayList<>())
                .paymentMethods(new ArrayList<>())
                .build();
    }

    /**
     * Creates a new User with a Cart
     */
    public static User createUserWithCart(String username, String email, String password,
                                          String firstName, String lastName, Role role) {

        User user = createUser(username, email, password, firstName, lastName, null, role);

        // Create and associate a cart
        Cart cart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user.setCart(cart);

        return user;
    }

    /**
     * Creates an admin user
     */
    public static User createAdminUser(String username, String email, String password,
                                       String firstName, String lastName, Role adminRole) {

        if (adminRole == null || !adminRole.getRoleName().equalsIgnoreCase("ADMIN")) {
            throw new IllegalArgumentException("Admin role is required");
        }

        return createUserWithCart(username, email, password, firstName, lastName, adminRole);
    }

    /**
     * Creates a customer user
     */
    public static User createCustomerUser(String username, String email, String password,
                                          String firstName, String lastName, Role customerRole) {

        if (customerRole == null || !customerRole.getRoleName().equalsIgnoreCase("CUSTOMER")) {
            throw new IllegalArgumentException("Customer role is required");
        }

        return createUserWithCart(username, email, password, firstName, lastName, customerRole);
    }
}