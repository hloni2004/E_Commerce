package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Permission;
import za.ac.styling.domain.Role;
import za.ac.styling.repository.RoleRepository;
import za.ac.styling.service.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Role entity
 */
@Service
public class RoleServiceImpl implements RoleService {

    private RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role create(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Role read(Integer id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    public Role update(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    @Override
    public boolean existsByRoleName(String roleName) {
        return roleRepository.existsByRoleName(roleName);
    }

    @Override
    public Role addPermission(Integer roleId, Permission permission) {
        Role role = read(roleId);
        if (role != null) {
            if (role.getPermissions() == null) {
                role.setPermissions(new ArrayList<>());
            }
            if (!role.getPermissions().contains(permission)) {
                role.getPermissions().add(permission);
            }
            return update(role);
        }
        return null;
    }

    @Override
    public Role removePermission(Integer roleId, Permission permission) {
        Role role = read(roleId);
        if (role != null && role.getPermissions() != null) {
            role.getPermissions().remove(permission);
            return update(role);
        }
        return null;
    }

    @Override
    public List<Permission> getPermissions(Integer roleId) {
        Role role = read(roleId);
        return (role != null && role.getPermissions() != null) ? role.getPermissions() : new ArrayList<>();
    }
}
