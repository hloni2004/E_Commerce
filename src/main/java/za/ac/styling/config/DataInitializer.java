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
        normalizeExistingEmails();
        initializeAdminUser();
    }

    private void initializeRoles() {
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("CUSTOMER");
        System.out.println("✓ Roles initialized successfully");
    }

    private void cleanupOldCategories() {
        try {

            categoryService.findByName("Luxury").ifPresent(category -> {
                categoryService.delete(category.getCategoryId());
                System.out.println("✓ Removed old category: Luxury");
            });
        } catch (Exception e) {
            System.out.println("  No old categories to clean up");
        }
    }

    private void normalizeExistingEmails() {
        try {
            var users = userService.getAll();
            int normalizedCount = 0;
            for (User user : users) {
                String originalEmail = user.getEmail();
                String normalizedEmail = originalEmail.toLowerCase();
                if (!originalEmail.equals(normalizedEmail)) {
                    user.setEmail(normalizedEmail);
                    userService.update(user);
                    normalizedCount++;
                }
            }
            if (normalizedCount > 0) {
                System.out.println("✓ Normalized " + normalizedCount + " user email(s) to lowercase");
            }
        } catch (Exception e) {
            System.out.println("  Could not normalize emails: " + e.getMessage());
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

    private void initializeAdminUser() {

        Role adminRole = roleService.findByRoleName("ADMIN")
            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        Optional<User> existingAdmin = userService.findByEmail("alexnhlanhla62@gmail.com");

        if (existingAdmin.isEmpty()) {

            User adminUser = za.ac.styling.factory.UserFactory.createAdminUser(
                    "admin",
                    "alexnhlanhla62@gmail.com",
                    "Admin@123",
                    "Admin",
                    "User",
                    adminRole
            );

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
