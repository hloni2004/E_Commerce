package za.ac.styling.factory;

import za.ac.styling.domain.Permission;
import za.ac.styling.util.ValidationHelper;

/**
 * Factory class responsible for creating Permission objects
 */
public class PermissionFactory {

    /**
     * Creates a new Permission with the specified name
     */
    public static Permission createPermission(String name) {

        // Validate input data
        if (ValidationHelper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Permission name cannot be empty");
        }

        // Normalize permission name to uppercase with underscores
        String normalizedName = name.trim().toUpperCase().replace(" ", "_");

        return Permission.builder()
                .name(normalizedName)
                .build();
    }

    /**
     * Creates a READ permission
     */
    public static Permission createReadPermission() {
        return createPermission("READ");
    }

    /**
     * Creates a WRITE permission
     */
    public static Permission createWritePermission() {
        return createPermission("WRITE");
    }

    /**
     * Creates a DELETE permission
     */
    public static Permission createDeletePermission() {
        return createPermission("DELETE");
    }

    /**
     * Creates an UPDATE permission
     */
    public static Permission createUpdatePermission() {
        return createPermission("UPDATE");
    }

    /**
     * Creates a MANAGE_USERS permission
     */
    public static Permission createManageUsersPermission() {
        return createPermission("MANAGE_USERS");
    }

    /**
     * Creates a MANAGE_PRODUCTS permission
     */
    public static Permission createManageProductsPermission() {
        return createPermission("MANAGE_PRODUCTS");
    }

    /**
     * Creates a MANAGE_ORDERS permission
     */
    public static Permission createManageOrdersPermission() {
        return createPermission("MANAGE_ORDERS");
    }

    /**
     * Creates a VIEW_REPORTS permission
     */
    public static Permission createViewReportsPermission() {
        return createPermission("VIEW_REPORTS");
    }
}
