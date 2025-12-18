package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.Role;
import za.ac.styling.domain.User;
import za.ac.styling.dto.LoginRequest;
import za.ac.styling.dto.RegisterRequest;
import za.ac.styling.dto.UserResponse;
import za.ac.styling.service.UserService;
import za.ac.styling.service.PasswordResetService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private za.ac.styling.security.JwtUtil jwtUtil;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

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

            // Generate tokens and set HttpOnly cookies
            String subject = String.valueOf(user.getUserId());
            String accessToken = jwtUtil.generateAccessToken(subject);
            String refreshToken = jwtUtil.generateRefreshToken(subject);

            var accessCookie = org.springframework.http.ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(900) // 15 minutes
                .sameSite("Lax")
                .build();

            var refreshCookie = org.springframework.http.ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/users/refresh")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Lax")
                .build();

            return ResponseEntity.ok().header("Set-Cookie", accessCookie.toString()).header("Set-Cookie", refreshCookie.toString()).body(userResponse);
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

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Invalid refresh token"));
        }
        String subject = jwtUtil.getSubject(refreshToken);
        String newAccess = jwtUtil.generateAccessToken(subject);
        String newRefresh = jwtUtil.generateRefreshToken(subject); // rotation - in production also revoke old

        var accessCookie = org.springframework.http.ResponseCookie.from("access_token", newAccess)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(900) // 15 minutes
            .sameSite("Lax")
            .build();

        var refreshCookie = org.springframework.http.ResponseCookie.from("refresh_token", newRefresh)
            .httpOnly(true)
            .secure(false)
            .path("/api/users/refresh")
            .maxAge(7 * 24 * 60 * 60)
            .sameSite("Lax")
            .build();

        return ResponseEntity.ok().header("Set-Cookie", accessCookie.toString()).header("Set-Cookie", refreshCookie.toString()).body(Map.of("success", true));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        var accessCookie = org.springframework.http.ResponseCookie.from("access_token", "")
            .httpOnly(true).secure(false).path("/").maxAge(0).sameSite("Lax").build();
        var refreshCookie = org.springframework.http.ResponseCookie.from("refresh_token", "")
            .httpOnly(true).secure(false).path("/api/users/refresh").maxAge(0).sameSite("Lax").build();
        return ResponseEntity.ok().header("Set-Cookie", accessCookie.toString()).header("Set-Cookie", refreshCookie.toString()).body(Map.of("success", true, "message", "Logged out"));
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Create and send password reset token
            String token = passwordResetService.createPasswordResetToken(email, frontendUrl);

            // Always return success to prevent email enumeration
            response.put("success", true);
            response.put("message", "If an account exists with this email, a password reset link has been sent. Check the server console for the reset link.");
            
            // FOR DEVELOPMENT: Include token in response
            if (token != null) {
                response.put("token", token);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error processing password reset request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/validate-reset-token/{token}")
    public ResponseEntity<?> validateResetToken(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isValid = passwordResetService.validateToken(token);
            
            if (isValid) {
                response.put("success", true);
                response.put("message", "Token is valid");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired reset token");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error validating token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            if (token == null || newPassword == null) {
                response.put("success", false);
                response.put("message", "Token and new password are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (newPassword.length() < 8) {
                response.put("success", false);
                response.put("message", "Password must be at least 8 characters");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            boolean success = passwordResetService.resetPassword(token, newPassword);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Password has been reset successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired reset token, or OTP not verified");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error resetting password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyResetOTP(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = request.get("token");
            String otpCode = request.get("otpCode");
            
            if (token == null || otpCode == null) {
                response.put("success", false);
                response.put("message", "Token and OTP code are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            boolean verified = passwordResetService.verifyOTP(token, otpCode);
            
            if (verified) {
                response.put("success", true);
                response.put("message", "OTP verified successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired OTP code");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error verifying OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

