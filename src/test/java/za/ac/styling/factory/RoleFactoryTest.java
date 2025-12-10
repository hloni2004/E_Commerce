package za.ac.styling.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Permission;
import za.ac.styling.domain.Role;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleFactoryTest {

    private Permission readPermission;
    private Permission writePermission;
    private List<Permission> permissions;

    @BeforeEach
    void setUp() {
        readPermission = Permission.builder()
                .permissionId(1L)
                .name("READ")
                .build();

        writePermission = Permission.builder()
                .permissionId(2L)
                .name("WRITE")
                .build();

        permissions = new ArrayList<>();
        permissions.add(readPermission);
        permissions.add(writePermission);
    }

    @Test
    void createRole_WithValidData_ShouldCreateRole() {
        // Arrange
        String roleName = "MANAGER";

        // Act
        Role role = RoleFactory.createRole(roleName, permissions);

        // Assert
        assertNotNull(role);
        assertEquals(roleName, role.getRoleName());
        assertEquals(2, role.getPermissions().size());
    }

    @Test
    void createRole_ShouldNormalizeToUppercase() {
        // Arrange
        String roleName = "manager";

        // Act
        Role role = RoleFactory.createRole(roleName);

        // Assert
        assertEquals("MANAGER", role.getRoleName());
    }

    @Test
    void createRole_WithEmptyName_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RoleFactory.createRole("", permissions)
        );

        assertEquals("Role name cannot be empty", exception.getMessage());
    }

    @Test
    void createRole_WithNullPermissions_ShouldCreateEmptyList() {
        // Act
        Role role = RoleFactory.createRole("TEST", null);

        // Assert
        assertNotNull(role.getPermissions());
        assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void createRole_WithoutPermissions_ShouldCreateRoleWithEmptyList() {
        // Act
        Role role = RoleFactory.createRole("TEST");

        // Assert
        assertNotNull(role);
        assertNotNull(role.getPermissions());
        assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void createAdminRole_ShouldCreateAdminRole() {
        // Act
        Role role = RoleFactory.createAdminRole(permissions);

        // Assert
        assertNotNull(role);
        assertEquals("ADMIN", role.getRoleName());
        assertEquals(permissions, role.getPermissions());
    }

    @Test
    void createCustomerRole_ShouldCreateCustomerRole() {
        // Act
        Role role = RoleFactory.createCustomerRole(readPermission);

        // Assert
        assertNotNull(role);
        assertEquals("CUSTOMER", role.getRoleName());
        assertEquals(1, role.getPermissions().size());
    }

    @Test
    void createManagerRole_ShouldCreateManagerRole() {
        // Act
        Role role = RoleFactory.createManagerRole(readPermission, writePermission);

        // Assert
        assertNotNull(role);
        assertEquals("MANAGER", role.getRoleName());
        assertEquals(2, role.getPermissions().size());
    }

    @Test
    void createVendorRole_ShouldCreateVendorRole() {
        // Act
        Role role = RoleFactory.createVendorRole(readPermission);

        // Assert
        assertNotNull(role);
        assertEquals("VENDOR", role.getRoleName());
    }

    @Test
    void createGuestRole_ShouldCreateGuestRole() {
        // Act
        Role role = RoleFactory.createGuestRole();

        // Assert
        assertNotNull(role);
        assertEquals("GUEST", role.getRoleName());
        assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void addPermission_ShouldAddPermissionToRole() {
        // Arrange
        Role role = RoleFactory.createRole("TEST");
        
        // Act
        Role updatedRole = RoleFactory.addPermission(role, readPermission);

        // Assert
        assertEquals(1, updatedRole.getPermissions().size());
        assertTrue(updatedRole.getPermissions().contains(readPermission));
    }

    @Test
    void addPermission_WithNullRole_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RoleFactory.addPermission(null, readPermission)
        );

        assertEquals("Role cannot be null", exception.getMessage());
    }

    @Test
    void addPermission_WithNullPermission_ShouldThrowException() {
        // Arrange
        Role role = RoleFactory.createRole("TEST");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RoleFactory.addPermission(role, null)
        );

        assertEquals("Permission cannot be null", exception.getMessage());
    }

    @Test
    void addPermission_DuplicatePermission_ShouldNotAddTwice() {
        // Arrange
        Role role = RoleFactory.createRole("TEST", permissions);
        int initialSize = role.getPermissions().size();

        // Act
        RoleFactory.addPermission(role, readPermission);

        // Assert
        assertEquals(initialSize, role.getPermissions().size());
    }

    @Test
    void removePermission_ShouldRemovePermissionFromRole() {
        // Arrange
        Role role = RoleFactory.createRole("TEST", permissions);

        // Act
        Role updatedRole = RoleFactory.removePermission(role, readPermission);

        // Assert
        assertEquals(1, updatedRole.getPermissions().size());
        assertFalse(updatedRole.getPermissions().contains(readPermission));
    }

    @Test
    void removePermission_WithNullRole_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RoleFactory.removePermission(null, readPermission)
        );

        assertEquals("Role cannot be null", exception.getMessage());
    }
}
