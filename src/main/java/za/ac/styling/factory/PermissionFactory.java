package za.ac.styling.factory;

import za.ac.styling.domain.Permission;
import za.ac.styling.util.ValidationHelper;

public class PermissionFactory {

    public static Permission createPermission(String name) {

        if (ValidationHelper.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Permission name cannot be empty");
        }

        String normalizedName = name.trim().toUpperCase().replace(" ", "_");

        return Permission.builder()
                .name(normalizedName)
                .build();
    }

    public static Permission createReadPermission() {
        return createPermission("READ");
    }

    public static Permission createWritePermission() {
        return createPermission("WRITE");
    }

    public static Permission createDeletePermission() {
        return createPermission("DELETE");
    }

    public static Permission createUpdatePermission() {
        return createPermission("UPDATE");
    }

    public static Permission createManageUsersPermission() {
        return createPermission("MANAGE_USERS");
    }

    public static Permission createManageProductsPermission() {
        return createPermission("MANAGE_PRODUCTS");
    }

    public static Permission createManageOrdersPermission() {
        return createPermission("MANAGE_ORDERS");
    }

    public static Permission createViewReportsPermission() {
        return createPermission("VIEW_REPORTS");
    }
}
