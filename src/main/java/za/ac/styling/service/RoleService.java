package za.ac.styling.service;

import za.ac.styling.domain.Permission;
import za.ac.styling.domain.Role;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Role entity
 */
public interface RoleService extends IService<Role, Integer> {

    /**
     * Find role by role name
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * Check if role name exists
     */
    boolean existsByRoleName(String roleName);

    /**
     * Add permission to role
     */
    Role addPermission(Integer roleId, Permission permission);

    /**
     * Remove permission from role
     */
    Role removePermission(Integer roleId, Permission permission);

    /**
     * Get all permissions for a role
     */
    List<Permission> getPermissions(Integer roleId);
}
