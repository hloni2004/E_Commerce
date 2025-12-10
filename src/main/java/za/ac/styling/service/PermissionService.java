package za.ac.styling.service;

import za.ac.styling.domain.Permission;

import java.util.Optional;

/**
 * Service interface for Permission entity
 */
public interface PermissionService extends IService<Permission, Long> {

    /**
     * Find permission by name
     */
    Optional<Permission> findByName(String name);

    /**
     * Check if permission name exists
     */
    boolean existsByName(String name);
}
