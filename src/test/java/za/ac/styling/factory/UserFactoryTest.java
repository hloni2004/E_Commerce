package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;

import static org.junit.jupiter.api.Assertions.*;

class UserFactoryTest {

    private Role customerRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Create test roles
        customerRole = Role.builder()
                .roleId(1)
                .roleName("CUSTOMER")
                .build();

        adminRole = Role.builder()
                .roleId(2)
                .roleName("ADMIN")
                .build();
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        // Arrange
        String username = "john_doe";
        String email = "john@example.com";
        String password = "Password123";
        String firstName = "John";
        String lastName = "Doe";
        String phone = "+27123456789";

        // Act
        User user = UserFactory.createUser(username, email, password, firstName, lastName, phone, customerRole);

        // Assert
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email.toLowerCase(), user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(phone, user.getPhone());
        assertEquals(customerRole, user.getRole());
        assertTrue(user.isActive());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getAddresses());
        assertTrue(user.getAddresses().isEmpty());
        assertNotNull(user.getPaymentMethods());
        assertTrue(user.getPaymentMethods().isEmpty());
        System.out.println("Created User: " + user);
    }

    @Test
    void createUser_WithNullPhone_ShouldCreateUser() {
        // Arrange
        String username = "jane_doe";
        String email = "jane@example.com";
        String password = "SecurePass123";
        String firstName = "Jane";
        String lastName = "Doe";

        // Act
        User user = UserFactory.createUser(username, email, password, firstName, lastName, null, customerRole);

        // Assert
        assertNotNull(user);
        assertNull(user.getPhone());
        assertEquals(username, user.getUsername());
    }

    @Test
    void createUser_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        String invalidUsername = "ab"; // Too short

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createUser(invalidUsername, "test@example.com", "Password123",
                        "John", "Doe", null, customerRole)
        );

        assertEquals("Invalid username", exception.getMessage());
    }

    @Test
    void createUser_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        String invalidEmail = "invalid-email";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createUser("john_doe", invalidEmail, "Password123",
                        "John", "Doe", null, customerRole)
        );

        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void createUser_WithShortPassword_ShouldThrowException() {
        // Arrange
        String shortPassword = "Pass12"; // Less than 8 characters

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createUser("john_doe", "john@example.com", shortPassword,
                        "John", "Doe", null, customerRole)
        );

        assertEquals("Password must be at least 8 characters long", exception.getMessage());
    }

    @Test
    void createUser_WithInvalidFirstName_ShouldThrowException() {
        // Arrange
        String invalidFirstName = "J"; // Too short

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createUser("john_doe", "john@example.com", "Password123",
                        invalidFirstName, "Doe", null, customerRole)
        );

        assertEquals("Invalid name format", exception.getMessage());
    }

    @Test
    void createUser_WithInvalidLastName_ShouldThrowException() {
        // Arrange
        String invalidLastName = "D"; // Too short

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createUser("john_doe", "john@example.com", "Password123",
                        "John", invalidLastName, null, customerRole)
        );

        assertEquals("Invalid name format", exception.getMessage());
    }

    @Test
    void createUser_WithInvalidPhone_ShouldThrowException() {
        // Arrange
        String invalidPhone = "abc123"; // Invalid format

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createUser("john_doe", "john@example.com", "Password123",
                        "John", "Doe", invalidPhone, customerRole)
        );

        assertEquals("Invalid phone number format", exception.getMessage());
    }

    @Test
    void createUser_EmailShouldBeLowercase() {
        // Arrange
        String mixedCaseEmail = "John.Doe@EXAMPLE.COM";

        // Act
        User user = UserFactory.createUser("john_doe", mixedCaseEmail, "Password123",
                "John", "Doe", null, customerRole);

        // Assert
        assertEquals(mixedCaseEmail.toLowerCase(), user.getEmail());
    }

    @Test
    void createUserWithCart() {
        // Arrange
        String username = "user_with_cart";
        String email = "cart@example.com";
        String password = "Password123";
        String firstName = "Cart";
        String lastName = "User";

        // Act
        User user = UserFactory.createUserWithCart(username, email, password, firstName, lastName, customerRole);

        // Assert
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertNotNull(user.getCart());
        assertEquals(user, user.getCart().getUser());
        assertNotNull(user.getCart().getItems());
        assertTrue(user.getCart().getItems().isEmpty());
        assertNotNull(user.getCart().getCreatedAt());
        assertNotNull(user.getCart().getUpdatedAt());
    }

    @Test
    void createUserWithCart_ShouldNotHavePhone() {
        // Act
        User user = UserFactory.createUserWithCart("testuser", "test@example.com",
                "Password123", "Test", "User", customerRole);

        // Assert
        assertNull(user.getPhone());
    }

    @Test
    void createAdminUser() {
        // Arrange
        String username = "admin_user";
        String email = "admin@example.com";
        String password = "AdminPass123";
        String firstName = "Admin";
        String lastName = "User";

        // Act
        User admin = UserFactory.createAdminUser(username, email, password, firstName, lastName, adminRole);

        // Assert
        assertNotNull(admin);
        assertEquals(username, admin.getUsername());
        assertEquals(email.toLowerCase(), admin.getEmail());
        assertEquals(firstName, admin.getFirstName());
        assertEquals(lastName, admin.getLastName());
        assertEquals(adminRole, admin.getRole());
        assertEquals("ADMIN", admin.getRole().getRoleName());
        assertNotNull(admin.getCart());
        assertTrue(admin.isActive());
    }

    @Test
    void createAdminUser_WithNullRole_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdminUser("admin", "admin@example.com", "Password123",
                        "Admin", "User", null)
        );

        assertEquals("Admin role is required", exception.getMessage());
    }

    @Test
    void createAdminUser_WithCustomerRole_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdminUser("admin", "admin@example.com", "Password123",
                        "Admin", "User", customerRole)
        );

        assertEquals("Admin role is required", exception.getMessage());
    }

    @Test
    void createCustomerUser() {
        // Arrange
        String username = "customer_user";
        String email = "customer@example.com";
        String password = "CustomerPass123";
        String firstName = "Customer";
        String lastName = "User";

        // Act
        User customer = UserFactory.createCustomerUser(username, email, password, firstName, lastName, customerRole);

        // Assert
        assertNotNull(customer);
        assertEquals(username, customer.getUsername());
        assertEquals(email.toLowerCase(), customer.getEmail());
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
        assertEquals(customerRole, customer.getRole());
        assertEquals("CUSTOMER", customer.getRole().getRoleName());
        assertNotNull(customer.getCart());
        assertTrue(customer.isActive());
    }

    @Test
    void createCustomerUser_WithNullRole_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomerUser("customer", "customer@example.com", "Password123",
                        "Customer", "User", null)
        );

        assertEquals("Customer role is required", exception.getMessage());
    }

    @Test
    void createCustomerUser_WithAdminRole_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomerUser("customer", "customer@example.com", "Password123",
                        "Customer", "User", adminRole)
        );

        assertEquals("Customer role is required", exception.getMessage());
    }

    @Test
    void createUser_WithValidPhoneFormats_ShouldCreateUser() {
        // Test various valid phone formats
        String[] validPhones = {
                "+27123456789",
                "0123456789",
                "+1-555-123-4567",
                "(555) 123-4567"
        };

        for (String phone : validPhones) {
            User user = UserFactory.createUser("testuser", "test@example.com",
                    "Password123", "Test", "User", phone, customerRole);
            assertNotNull(user);
            assertEquals(phone, user.getPhone());
        }
    }

    @Test
    void createUser_ShouldInitializeEmptyLists() {
        // Act
        User user = UserFactory.createUser("testuser", "test@example.com",
                "Password123", "Test", "User", null, customerRole);

        // Assert
        assertNotNull(user.getAddresses());
        assertNotNull(user.getPaymentMethods());
        assertTrue(user.getAddresses().isEmpty());
        assertTrue(user.getPaymentMethods().isEmpty());
    }

    @Test
    void createUser_ShouldSetActiveByDefault() {
        // Act
        User user = UserFactory.createUser("testuser", "test@example.com",
                "Password123", "Test", "User", null, customerRole);

        // Assert
        assertTrue(user.isActive());
    }

    @Test
    void createAdminUser_RoleNameCaseInsensitive() {
        // Create role with lowercase
        Role adminRoleLower = Role.builder()
                .roleId(3)
                .roleName("admin")
                .build();

        // Act
        User admin = UserFactory.createAdminUser("admin", "admin@example.com",
                "Password123", "Admin", "User", adminRoleLower);

        // Assert
        assertNotNull(admin);
        assertEquals("admin", admin.getRole().getRoleName());
    }

    @Test
    void createCustomerUser_RoleNameCaseInsensitive() {
        // Create role with mixed case
        Role customerRoleMixed = Role.builder()
                .roleId(4)
                .roleName("CuStOmEr")
                .build();

        // Act
        User customer = UserFactory.createCustomerUser("customer", "customer@example.com",
                "Password123", "Customer", "User", customerRoleMixed);

        // Assert
        assertNotNull(customer);
        assertEquals("CuStOmEr", customer.getRole().getRoleName());
    }
}