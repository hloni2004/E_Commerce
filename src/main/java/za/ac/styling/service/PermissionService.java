package za.ac.styling.service;

import za.ac.styling.domain.Permission;

import java.util.Optional;

public interface PermissionService extends IService<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);
}
