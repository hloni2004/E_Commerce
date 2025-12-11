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

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeCategories();
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

    /**
     * Creates default categories if they don't exist
     */
    private void initializeCategories() {
        createCategoryIfNotExists("Men's Clothing", "Fashion for men", "https://images.unsplash.com/photo-1490114538077-0a7f8cb49891?w=400");
        createCategoryIfNotExists("Women's Clothing", "Fashion for women", "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=400");
        createCategoryIfNotExists("Accessories", "Bags, watches, jewelry", "https://images.unsplash.com/photo-1492707892479-7bc8d5a4ee93?w=400");
        createCategoryIfNotExists("Footwear", "Shoes and sneakers", "https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=400");
        createCategoryIfNotExists("Luxury", "Premium and designer items", "https://images.unsplash.com/photo-1591348278863-96b5e6c379db?w=400");
        System.out.println("✓ Categories initialized successfully");
    }

    private void createCategoryIfNotExists(String name, String description, String imageUrl) {
        try {
            // Check if category exists (you may need to implement this in CategoryService)
            Category category = Category.builder()
                    .name(name)
                    .description(description)
                    .imageUrl(imageUrl)
                    .isActive(true)
                    .build();
            categoryService.create(category);
            System.out.println("✓ Created category: " + name);
        } catch (Exception e) {
            // Category might already exist, skip
            System.out.println("  Category already exists: " + name);
        }
    }

    /**
     * Creates default admin user if no admin exists
     */
    private void initializeAdminUser() {
        // Check if any admin user exists
        Role adminRole = roleService.findByRoleName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        // Check if admin user already exists
        Optional<User> existingAdmin = userService.findByEmail("admin@maison-luxe.com");
        
        if (existingAdmin.isEmpty()) {
            // Create default admin user
            User adminUser = User.builder()
                    .email("admin@maison-luxe.com")
                    .password("Admin@123")  // TODO: In production, this should be hashed
                    .firstName("Admin")
                    .lastName("User")
                    .username("admin")
                    .role(adminRole)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();

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
