package za.ac.styling.service;

import za.ac.styling.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends IService<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findActiveUsers();

    List<User> findInactiveUsers();

    User activateUser(Integer userId);

    User deactivateUser(Integer userId);
}
