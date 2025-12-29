package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.PromoCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Integer> {

    /**
     * Find promo code by unique code string
     */
    Optional<PromoCode> findByCode(String code);

    /**
     * Find promo code by code (case-insensitive)
     */
    Optional<PromoCode> findByCodeIgnoreCase(String code);

    /**
     * Find all active promo codes
     */
    List<PromoCode> findByIsActiveTrue();

    /**
     * Find valid promo codes (active, within date range, under usage limit)
     */
    @Query("SELECT p FROM PromoCode p WHERE p.isActive = true " +
           "AND (p.startDate IS NULL OR p.startDate <= :now) " +
           "AND (p.endDate IS NULL OR p.endDate >= :now) " +
           "AND (p.usageLimit IS NULL OR p.currentUsage < p.usageLimit)")
    List<PromoCode> findValidPromoCodes(@Param("now") LocalDateTime now);

    /**
     * Check if code exists (case-insensitive)
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Atomically increment usage count only if usage limit not reached (or usage_limit is null).
     * Returns number of rows updated (1 if incremented, 0 otherwise).
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE PromoCode p SET p.currentUsage = p.currentUsage + 1 WHERE p.promoId = :id AND (p.usageLimit IS NULL OR p.currentUsage < p.usageLimit)")
    int incrementUsageIfBelowLimit(@org.springframework.data.repository.query.Param("id") Integer id);
}
