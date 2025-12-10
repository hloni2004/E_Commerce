package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.User;
import za.ac.styling.repository.UserRepository;
import za.ac.styling.service.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for User entity
 */
@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    public User read(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<User> findActiveUsers() {
        return (List<User>) userRepository.findByIsActiveTrue();
    }

    @Override
    public List<User> findInactiveUsers() {
        return (List<User>) userRepository.findByIsActiveFalse();
    }

    @Override
    public User activateUser(Integer userId) {
        User user = read(userId);
        if (user != null) {
            user.setActive(true);
            return update(user);
        }
        return null;
    }

    @Override
    public User deactivateUser(Integer userId) {
        User user = read(userId);
        if (user != null) {
            user.setActive(false);
            return update(user);
        }
        return null;
    }
}
