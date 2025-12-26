package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.PasswordResetToken;
import za.ac.styling.domain.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiryDateAfter(User user, LocalDateTime now);

    void deleteByExpiryDateBefore(LocalDateTime now);

    void deleteByUser(User user);

    java.util.List<PasswordResetToken> findTop50ByOrderByCreatedAtDesc();
}
