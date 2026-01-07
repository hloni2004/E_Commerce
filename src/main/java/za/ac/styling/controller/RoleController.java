package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.Role;
import za.ac.styling.service.RoleService;
import za.ac.styling.util.ResponseUtil;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        try {
            Role created = roleService.create(role);
            return ResponseUtil.created(created);
        } catch (Exception e) {
            return ResponseUtil.error("Error creating role", e);
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Integer id) {
        try {
            Role role = roleService.read(id);
            return role != null ? ResponseUtil.success(role) : ResponseUtil.notFound("Role not found");
        } catch (Exception e) {
            return ResponseUtil.error("Error retrieving role", e);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody Role role) {
        try {
            Role updated = roleService.update(role);
            return updated != null ? ResponseUtil.success(updated) : ResponseUtil.notFound("Role not found");
        } catch (Exception e) {
            return ResponseUtil.error("Error updating role", e);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<Role> roles = roleService.getAll();
            return ResponseUtil.success(roles);
        } catch (Exception e) {
            return ResponseUtil.error("Error retrieving roles", e);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Integer id) {
        try {
            roleService.delete(id);
            return ResponseUtil.success("Role deleted successfully");
        } catch (Exception e) {
            return ResponseUtil.error("Error deleting role", e);
        }
    }

    @GetMapping("/name/{roleName}")
    public ResponseEntity<?> getRoleByName(@PathVariable String roleName) {
        try {
            Role role = roleService.findByRoleName(roleName).orElse(null);
            return role != null ? ResponseUtil.success(role) : ResponseUtil.notFound("Role not found");
        } catch (Exception e) {
            return ResponseUtil.error("Error retrieving role", e);
        }
    }
}
