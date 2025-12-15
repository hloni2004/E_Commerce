package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.PromoCode;
import za.ac.styling.domain.PromoUsage;
import za.ac.styling.domain.User;

import java.util.List;

@Repository
public interface PromoUsageRepository extends JpaRepository<PromoUsage, Integer> {

    /**
     * Find all usage records for a specific promo code
     */
    List<PromoUsage> findByPromoCode(PromoCode promoCode);

    /**
     * Find all usage records for a specific user
     */
    List<PromoUsage> findByUser(User user);

    /**
     * Check if a user has already used a specific promo code
     */
    boolean existsByPromoCodeAndUser(PromoCode promoCode, User user);

    /**
     * Count how many times a user has used a specific promo
     */
    @Query("SELECT COUNT(pu) FROM PromoUsage pu WHERE pu.promoCode.promoId = :promoId AND pu.user.userId = :userId")
    long countByPromoIdAndUserId(@Param("promoId") Integer promoId, @Param("userId") Integer userId);

    /**
     * Count total usage for a promo code
     */
    long countByPromoCode(PromoCode promoCode);

    /**
     * Find usage by promo code and user
     */
    List<PromoUsage> findByPromoCodeAndUser(PromoCode promoCode, User user);
}
