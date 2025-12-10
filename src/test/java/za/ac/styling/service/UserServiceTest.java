package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;
import za.ac.styling.factory.RoleFactory;
import za.ac.styling.factory.UserFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = RoleFactory.createRole("CUSTOMER");
        testRole = roleService.create(testRole);

        testUser = UserFactory.createUser(
                "johndoe",
                "john.doe@example.com",
                "password123",
                "John",
                "Doe",
                "1234567890",
                testRole
        );
    }

    @Test
    void testCreate() {
        User created = userService.create(testUser);
        assertNotNull(created);
        assertNotNull(created.getUserId());
        assertEquals("john.doe@example.com", created.getEmail());
    }

    @Test
    void testRead() {
        User created = userService.create(testUser);
        User found = userService.read(created.getUserId());
        assertNotNull(found);
        assertEquals(created.getUserId(), found.getUserId());
        assertEquals(created.getEmail(), found.getEmail());
    }

    @Test
    void testUpdate() {
        User created = userService.create(testUser);
        created.setFirstName("Jane");
        User updated = userService.update(created);
        assertNotNull(updated);
        assertEquals("Jane", updated.getFirstName());
    }

    @Test
    void testGetAll() {
        userService.create(testUser);
        User user2 = UserFactory.createUser(
                "janedoe",
                "jane.doe@example.com",
                "password456",
                "Jane",
                "Doe",
                "0987654321",
                testRole
        );
        userService.create(user2);

        List<User> users = userService.getAll();
        assertNotNull(users);
        assertTrue(users.size() >= 2);
    }

    @Test
    void testFindByUsername() {
        User created = userService.create(testUser);
        Optional<User> found = userService.findByUsername(created.getUsername());
        assertTrue(found.isPresent());
        assertEquals(created.getUsername(), found.get().getUsername());
    }

    @Test
    void testFindByEmail() {
        User created = userService.create(testUser);
        Optional<User> found = userService.findByEmail("john.doe@example.com");
        assertTrue(found.isPresent());
        assertEquals("john.doe@example.com", found.get().getEmail());
    }

    @Test
    void testExistsByUsername() {
        User created = userService.create(testUser);
        assertTrue(userService.existsByUsername(created.getUsername()));
        assertFalse(userService.existsByUsername("nonexistent"));
    }

    @Test
    void testExistsByEmail() {
        userService.create(testUser);
        assertTrue(userService.existsByEmail("john.doe@example.com"));
        assertFalse(userService.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testFindActiveUsers() {
        testUser.setActive(true);
        userService.create(testUser);

        User inactiveUser = UserFactory.createUser(
                "inactiveuser",
                "inactive@example.com",
                "password",
                "Inactive",
                "User",
                "5555555555",
                testRole
        );
        inactiveUser.setActive(false);
        userService.create(inactiveUser);

        List<User> activeUsers = userService.findActiveUsers();
        assertNotNull(activeUsers);
        assertTrue(activeUsers.stream().allMatch(User::isActive));
    }

    @Test
    void testActivateUser() {
        testUser.setActive(false);
        User created = userService.create(testUser);
        assertFalse(created.isActive());

        User activated = userService.activateUser(created.getUserId());
        assertNotNull(activated);
        assertTrue(activated.isActive());
    }

    @Test
    void testDeactivateUser() {
        testUser.setActive(true);
        User created = userService.create(testUser);
        assertTrue(created.isActive());

        User deactivated = userService.deactivateUser(created.getUserId());
        assertNotNull(deactivated);
        assertFalse(deactivated.isActive());
    }
}
