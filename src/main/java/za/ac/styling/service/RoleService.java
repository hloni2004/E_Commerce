package za.ac.styling.service;

import za.ac.styling.domain.Permission;
import za.ac.styling.domain.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService extends IService<Role, Integer> {

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    Role addPermission(Integer roleId, Permission permission);

    Role removePermission(Integer roleId, Permission permission);

    List<Permission> getPermissions(Integer roleId);
}
