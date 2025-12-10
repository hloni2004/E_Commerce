package za.ac.styling.factory;

import za.ac.styling.domain.Permission;
import za.ac.styling.domain.Role;
import za.ac.styling.util.ValidationHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factory class responsible for creating Role objects
 */
public class RoleFactory {

    /**
     * Creates a new Role with the specified name and permissions
     */
    public static Role createRole(String roleName, List<Permission> permissions) {

        // Validate input data
        if (ValidationHelper.isNullOrEmpty(roleName)) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }

        if (permissions == null) {
            permissions = new ArrayList<>();
        }

        // Normalize role name to uppercase
        String normalizedRoleName = roleName.trim().toUpperCase();

        return Role.builder()
                .roleName(normalizedRoleName)
                .permissions(permissions)
                .build();
    }

    /**
     * Creates a new Role without permissions
     */
    public static Role createRole(String roleName) {
        return createRole(roleName, new ArrayList<>());
    }

    /**
     * Creates an ADMIN role with all permissions
     */
    public static Role createAdminRole(List<Permission> allPermissions) {
        return createRole("ADMIN", allPermissions);
    }

    /**
     * Creates a CUSTOMER role with basic permissions
     */
    public static Role createCustomerRole(Permission... permissions) {
        List<Permission> permissionList = permissions != null ? 
                Arrays.asList(permissions) : new ArrayList<>();
        return createRole("CUSTOMER", permissionList);
    }

    /**
     * Creates a MANAGER role with management permissions
     */
    public static Role createManagerRole(Permission... permissions) {
        List<Permission> permissionList = permissions != null ? 
                Arrays.asList(permissions) : new ArrayList<>();
        return createRole("MANAGER", permissionList);
    }

    /**
     * Creates a VENDOR role with product management permissions
     */
    public static Role createVendorRole(Permission... permissions) {
        List<Permission> permissionList = permissions != null ? 
                Arrays.asList(permissions) : new ArrayList<>();
        return createRole("VENDOR", permissionList);
    }

    /**
     * Creates a GUEST role with minimal permissions
     */
    public static Role createGuestRole() {
        return createRole("GUEST", new ArrayList<>());
    }

    /**
     * Adds a permission to an existing role
     */
    public static Role addPermission(Role role, Permission permission) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }

        if (role.getPermissions() == null) {
            role.setPermissions(new ArrayList<>());
        }

        if (!role.getPermissions().contains(permission)) {
            role.getPermissions().add(permission);
        }

        return role;
    }

    /**
     * Removes a permission from an existing role
     */
    public static Role removePermission(Role role, Permission permission) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        if (permission != null && role.getPermissions() != null) {
            role.getPermissions().remove(permission);
        }

        return role;
    }
}
