package za.ac.styling.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import za.ac.styling.domain.Category;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;
import za.ac.styling.service.CategoryService;
import za.ac.styling.service.RoleService;
import za.ac.styling.service.UserService;
import za.ac.styling.service.CartService;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Initializes database with default admin user and roles on application startup
 */
@Component
public class DataInitializer implements CommandLineRunner {


    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        cleanupOldCategories();
        initializeAdminUser();
    }

    /**
     * Creates default roles if they don't exist
     */
    private void initializeRoles() {
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("CUSTOMER");
        System.out.println("✓ Roles initialized successfully");
    }

    /**
     * Removes old categories that are no longer needed
     */
    private void cleanupOldCategories() {
        try {
            // Remove "Luxury" category if it exists
            categoryService.findByName("Luxury").ifPresent(category -> {
                categoryService.delete(category.getCategoryId());
                System.out.println("✓ Removed old category: Luxury");
            });
        } catch (Exception e) {
            System.out.println("  No old categories to clean up");
        }
    }

    private void createRoleIfNotExists(String roleName) {
        Optional<Role> existingRole = roleService.findByRoleName(roleName);
        if (existingRole.isEmpty()) {
            Role role = Role.builder()
                    .roleName(roleName)
                    .build();
            roleService.create(role);
            System.out.println("✓ Created role: " + roleName);
        }
    }

        // Category initialization removed. Categories will be inserted by the user.

    /**
     * Creates default admin user if no admin exists
     */
    private void initializeAdminUser() {
        // Check if any admin user exists
        Role adminRole = roleService.findByRoleName("ADMIN")
            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        // Check if admin user already exists
        Optional<User> existingAdmin = userService.findByEmail("alexnhlanhla62@gmail.com");

        if (existingAdmin.isEmpty()) {
            // Create default admin user using UserFactory to hash password
            User adminUser = za.ac.styling.factory.UserFactory.createAdminUser(
                    "admin",
                    "alexnhlanhla62@gmail.com",
                    "Admin@123",
                    "Admin",
                    "User",
                    adminRole
            );

            // Ensure both sides of the relationship are set
            if (adminUser.getCart() != null) {
                adminUser.getCart().setUser(adminUser);
            }

            userService.create(adminUser);

            System.out.println("═══════════════════════════════════════════════");
            System.out.println("✓ DEFAULT ADMIN USER CREATED");
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("Email:    admin@maison-luxe.com");
            System.out.println("Password: Admin@123");
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("⚠ IMPORTANT: Change this password immediately!");
            System.out.println("═══════════════════════════════════════════════");
        } else {
            System.out.println("✓ Admin user already exists");
        }
    }
}
