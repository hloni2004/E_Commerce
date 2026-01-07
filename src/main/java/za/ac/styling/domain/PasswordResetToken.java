package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used;

    @Column(length = 6)
    private String otpCode;

    @Column(nullable = false)
    private boolean otpVerified;

    @Column(nullable = true)
    private boolean emailSent;

    @Column(nullable = true)
    private LocalDateTime lastSentAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public boolean isExpired() {

        return java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).isAfter(expiryDate);
    }
}
