package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Permission;
import za.ac.styling.factory.PermissionFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PermissionServiceTest {

    @Autowired
    private PermissionService permissionService;

    private Permission testPermission;

    @BeforeEach
    void setUp() {
        testPermission = PermissionFactory.createPermission("READ_" + System.currentTimeMillis());
    }

    @Test
    void testCreate() {
        Permission created = permissionService.create(testPermission);
        assertNotNull(created);
        assertNotNull(created.getPermissionId());
        assertTrue(created.getName().startsWith("READ_"));
    }

    @Test
    void testRead() {
        Permission created = permissionService.create(testPermission);
        Permission found = permissionService.read(created.getPermissionId());
        assertNotNull(found);
        assertEquals(created.getPermissionId(), found.getPermissionId());
    }

    @Test
    void testUpdate() {
        Permission created = permissionService.create(testPermission);
        created.setName("WRITE");
        Permission updated = permissionService.update(created);
        assertNotNull(updated);
        assertEquals("WRITE", updated.getName());
    }

    @Test
    void testGetAll() {
        permissionService.create(testPermission);
        List<Permission> permissions = permissionService.getAll();
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
    }

    @Test
    void testFindByName() {
        permissionService.create(testPermission);
        Optional<Permission> found = permissionService.findByName(testPermission.getName());
        assertTrue(found.isPresent());
        assertEquals(testPermission.getName(), found.get().getName());
    }

    @Test
    void testExistsByName() {
        permissionService.create(testPermission);
        assertTrue(permissionService.existsByName(testPermission.getName()));
        assertFalse(permissionService.existsByName("NONEXISTENT"));
    }
}
