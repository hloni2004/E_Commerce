package za.ac.styling.service;

import za.ac.styling.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for User entity
 */
public interface UserService extends IService<User, Integer> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all active users
     */
    List<User> findActiveUsers();

    /**
     * Find all inactive users
     */
    List<User> findInactiveUsers();

    /**
     * Activate user
     */
    User activateUser(Integer userId);

    /**
     * Deactivate user
     */
    User deactivateUser(Integer userId);
}
