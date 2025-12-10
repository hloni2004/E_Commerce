package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.Role;

import java.util.Optional;

/**
 * Repository interface for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Find role by name
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * Check if role name exists
     */
    boolean existsByRoleName(String roleName);
}
