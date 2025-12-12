package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;
import za.ac.styling.dto.LoginRequest;
import za.ac.styling.dto.RegisterRequest;
import za.ac.styling.dto.UserResponse;
import za.ac.styling.service.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private za.ac.styling.service.RoleService roleService;

    @Autowired
    private za.ac.styling.repository.AddressRepository addressRepository;

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User created = userService.create(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Integer id) {
        try {
            User user = userService.read(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving user: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody User user) {
        try {
            User updated = userService.update(user);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error updating user: " + e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        try {
            List<User> users = userService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving users: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            userService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error deleting user: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = userService.findByEmail(loginRequest.getEmail())
                .orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!user.isActive()) {
                response.put("success", false);
                response.put("message", "Account is inactive. Please contact support.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Note: In production, use proper password encryption/hashing (BCrypt)
            if (!user.getPassword().equals(loginRequest.getPassword())) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Convert to UserResponse DTO
            UserResponse userResponse = UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : "CUSTOMER")
                .isActive(user.isActive())
                .build();

            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate input
            if (registerRequest.getEmail() == null || registerRequest.getPassword() == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Check if user already exists
            if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
                response.put("success", false);
                response.put("message", "User with this email already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            if (registerRequest.getUsername() != null && 
                userService.findByUsername(registerRequest.getUsername()).isPresent()) {
                response.put("success", false);
                response.put("message", "Username already taken");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Fetch or create default role (CUSTOMER role)
            Role defaultRole = roleService.findByRoleName("CUSTOMER")
                .orElseGet(() -> {
                    // Create CUSTOMER role if it doesn't exist
                    Role newRole = Role.builder()
                        .roleName("CUSTOMER")
                        .build();
                    return roleService.create(newRole);
                });

            // Create new user
            User newUser = User.builder()
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())  // In production, hash this!
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .username(registerRequest.getUsername())
                .phone(registerRequest.getPhone())
                .role(defaultRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

            User created = userService.create(newUser);
            if (created == null) {
                response.put("success", false);
                response.put("message", "Failed to create user");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // Create default address if provided
            if (registerRequest.getAddressLine1() != null && !registerRequest.getAddressLine1().isEmpty()) {
                za.ac.styling.domain.Address address = za.ac.styling.domain.Address.builder()
                    .fullName(registerRequest.getFirstName() + " " + registerRequest.getLastName())
                    .phone(registerRequest.getPhone())
                    .addressLine1(registerRequest.getAddressLine1())
                    .addressLine2(registerRequest.getAddressLine2())
                    .city(registerRequest.getCity())
                    .province(registerRequest.getProvince())
                    .postalCode(registerRequest.getPostalCode())
                    .country(registerRequest.getCountry() != null ? registerRequest.getCountry() : "South Africa")
                    .addressType(za.ac.styling.domain.AddressType.SHIPPING)
                    .isDefault(true)
                    .user(created)
                    .build();
                
                addressRepository.save(address);
            }

            // Convert to UserResponse DTO
            UserResponse userResponse = UserResponse.builder()
                .userId(created.getUserId())
                .username(created.getUsername())
                .email(created.getEmail())
                .firstName(created.getFirstName())
                .lastName(created.getLastName())
                .phone(created.getPhone())
                .roleName(created.getRole() != null ? created.getRole().getRoleName() : "CUSTOMER")
                .isActive(created.isActive())
                .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.findByEmail(email)
                .orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving user: " + e.getMessage()));
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Integer userId, @RequestBody Map<String, Object> updates) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.read(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Update allowed fields
            if (updates.containsKey("firstName")) {
                user.setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                user.setLastName((String) updates.get("lastName"));
            }
            if (updates.containsKey("phone")) {
                user.setPhone((String) updates.get("phone"));
            }
            if (updates.containsKey("email")) {
                String newEmail = (String) updates.get("email");
                // Check if email is already taken by another user
                if (!user.getEmail().equals(newEmail) && 
                    userService.findByEmail(newEmail).isPresent()) {
                    response.put("success", false);
                    response.put("message", "Email already in use");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                }
                user.setEmail(newEmail);
            }

            User updated = userService.update(user);
            
            UserResponse userResponse = UserResponse.builder()
                .userId(updated.getUserId())
                .username(updated.getUsername())
                .email(updated.getEmail())
                .firstName(updated.getFirstName())
                .lastName(updated.getLastName())
                .phone(updated.getPhone())
                .roleName(updated.getRole() != null ? updated.getRole().getRoleName() : "CUSTOMER")
                .isActive(updated.isActive())
                .build();

            response.put("success", true);
            response.put("data", userResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/change-password/{userId}")
    public ResponseEntity<?> changePassword(@PathVariable Integer userId, @RequestBody Map<String, String> passwords) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.read(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String currentPassword = passwords.get("currentPassword");
            String newPassword = passwords.get("newPassword");

            if (!user.getPassword().equals(currentPassword)) {
                response.put("success", false);
                response.put("message", "Current password is incorrect");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            user.setPassword(newPassword);
            userService.update(user);

            response.put("success", true);
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error changing password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
