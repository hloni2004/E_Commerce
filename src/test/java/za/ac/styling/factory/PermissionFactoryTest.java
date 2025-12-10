package za.ac.styling.factory;

import org.junit.jupiter.api.Test;
import za.ac.styling.domain.Permission;

import static org.junit.jupiter.api.Assertions.*;

class PermissionFactoryTest {

    @Test
    void createPermission_WithValidName_ShouldCreatePermission() {
        // Arrange
        String name = "READ_PRODUCTS";

        // Act
        Permission permission = PermissionFactory.createPermission(name);

        // Assert
        assertNotNull(permission);
        assertEquals(name, permission.getName());
    }

    @Test
    void createPermission_ShouldNormalizeToUppercase() {
        // Arrange
        String name = "read products";

        // Act
        Permission permission = PermissionFactory.createPermission(name);

        // Assert
        assertEquals("READ_PRODUCTS", permission.getName());
    }

    @Test
    void createPermission_WithEmptyName_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PermissionFactory.createPermission("")
        );

        assertEquals("Permission name cannot be empty", exception.getMessage());
    }

    @Test
    void createPermission_WithNullName_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PermissionFactory.createPermission(null)
        );

        assertEquals("Permission name cannot be empty", exception.getMessage());
    }

    @Test
    void createReadPermission_ShouldCreateReadPermission() {
        // Act
        Permission permission = PermissionFactory.createReadPermission();

        // Assert
        assertNotNull(permission);
        assertEquals("READ", permission.getName());
    }

    @Test
    void createWritePermission_ShouldCreateWritePermission() {
        // Act
        Permission permission = PermissionFactory.createWritePermission();

        // Assert
        assertNotNull(permission);
        assertEquals("WRITE", permission.getName());
    }

    @Test
    void createDeletePermission_ShouldCreateDeletePermission() {
        // Act
        Permission permission = PermissionFactory.createDeletePermission();

        // Assert
        assertNotNull(permission);
        assertEquals("DELETE", permission.getName());
    }

    @Test
    void createUpdatePermission_ShouldCreateUpdatePermission() {
        // Act
        Permission permission = PermissionFactory.createUpdatePermission();

        // Assert
        assertNotNull(permission);
        assertEquals("UPDATE", permission.getName());
    }

    @Test
    void createManageUsersPermission_ShouldCreateManageUsersPermission() {
        // Act
        Permission permission = PermissionFactory.createManageUsersPermission();

        // Assert
        assertNotNull(permission);
        assertEquals("MANAGE_USERS", permission.getName());
    }

    @Test
    void createManageProductsPermission_ShouldCreateManageProductsPermission() {
        // Act
        Permission permission = PermissionFactory.createManageProductsPermission();

        // Assert
        assertNotNull(permission);
        assertEquals("MANAGE_PRODUCTS", permission.getName());
    }

    @Test
    void createManageOrdersPermission_ShouldCreateManageOrdersPermission() {
        // Act
        Permission permission = PermissionFactory.createManageOrdersPermission();

        // Assert
        assertNotNull(permission);
        assertEquals("MANAGE_ORDERS", permission.getName());
    }

    @Test
    void createViewReportsPermission_ShouldCreateViewReportsPermission() {
        // Act
        Permission permission = PermissionFactory.createViewReportsPermission();

        // Assert
        assertNotNull(permission);
        assertEquals("VIEW_REPORTS", permission.getName());
    }
}
