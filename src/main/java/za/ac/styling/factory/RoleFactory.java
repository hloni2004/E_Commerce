package za.ac.styling.factory;

import za.ac.styling.domain.Permission;
import za.ac.styling.domain.Role;
import za.ac.styling.util.ValidationHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoleFactory {

    public static Role createRole(String roleName, List<Permission> permissions) {

        if (ValidationHelper.isNullOrEmpty(roleName)) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }

        if (permissions == null) {
            permissions = new ArrayList<>();
        }

        String normalizedRoleName = roleName.trim().toUpperCase();

        return Role.builder()
                .roleName(normalizedRoleName)
                .permissions(permissions)
                .build();
    }

    public static Role createRole(String roleName) {
        return createRole(roleName, new ArrayList<>());
    }

    public static Role createAdminRole(List<Permission> allPermissions) {
        return createRole("ADMIN", allPermissions);
    }

    public static Role createCustomerRole(Permission... permissions) {
        List<Permission> permissionList = permissions != null ? 
                Arrays.asList(permissions) : new ArrayList<>();
        return createRole("CUSTOMER", permissionList);
    }

    public static Role createManagerRole(Permission... permissions) {
        List<Permission> permissionList = permissions != null ? 
                Arrays.asList(permissions) : new ArrayList<>();
        return createRole("MANAGER", permissionList);
    }

    public static Role createVendorRole(Permission... permissions) {
        List<Permission> permissionList = permissions != null ? 
                Arrays.asList(permissions) : new ArrayList<>();
        return createRole("VENDOR", permissionList);
    }

    public static Role createGuestRole() {
        return createRole("GUEST", new ArrayList<>());
    }

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
