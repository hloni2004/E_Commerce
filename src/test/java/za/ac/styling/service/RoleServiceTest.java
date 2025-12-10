package za.ac.styling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import za.ac.styling.domain.Permission;
import za.ac.styling.domain.Role;
import za.ac.styling.factory.PermissionFactory;
import za.ac.styling.factory.RoleFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        testRole = RoleFactory.createRole("ADMIN");
        testPermission = PermissionFactory.createPermission("READ");
        testPermission = permissionService.create(testPermission);
    }

    @Test
    void testCreate() {
        Role created = roleService.create(testRole);
        assertNotNull(created);
        assertNotNull(created.getRoleId());
        assertEquals("ADMIN", created.getRoleName());
    }

    @Test
    void testRead() {
        Role created = roleService.create(testRole);
        Role found = roleService.read(created.getRoleId());
        assertNotNull(found);
        assertEquals(created.getRoleId(), found.getRoleId());
    }

    @Test
    void testUpdate() {
        Role created = roleService.create(testRole);
        created.setRoleName("SUPER_ADMIN");
        Role updated = roleService.update(created);
        assertNotNull(updated);
        assertEquals("SUPER_ADMIN", updated.getRoleName());
    }

    @Test
    void testGetAll() {
        roleService.create(testRole);
        List<Role> roles = roleService.getAll();
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
    }

    @Test
    void testFindByRoleName() {
        roleService.create(testRole);
        Optional<Role> found = roleService.findByRoleName("ADMIN");
        assertTrue(found.isPresent());
        assertEquals("ADMIN", found.get().getRoleName());
    }

    @Test
    void testExistsByRoleName() {
        roleService.create(testRole);
        assertTrue(roleService.existsByRoleName("ADMIN"));
        assertFalse(roleService.existsByRoleName("NONEXISTENT"));
    }

    @Test
    void testAddPermission() {
        Role created = roleService.create(testRole);
        Role updated = roleService.addPermission(created.getRoleId(), testPermission);
        assertNotNull(updated);
        assertTrue(updated.getPermissions().contains(testPermission));
    }

    @Test
    void testRemovePermission() {
        Role created = roleService.create(testRole);
        roleService.addPermission(created.getRoleId(), testPermission);
        Role updated = roleService.removePermission(created.getRoleId(), testPermission);
        assertNotNull(updated);
        assertFalse(updated.getPermissions().contains(testPermission));
    }

    @Test
    void testGetPermissions() {
        Role created = roleService.create(testRole);
        roleService.addPermission(created.getRoleId(), testPermission);
        List<Permission> permissions = roleService.getPermissions(created.getRoleId());
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
    }
}
