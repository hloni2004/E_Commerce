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

    List<PromoUsage> findByPromoCode(PromoCode promoCode);

    List<PromoUsage> findByUser(User user);

    boolean existsByPromoCodeAndUser(PromoCode promoCode, User user);

    @Query("SELECT COUNT(pu) FROM PromoUsage pu WHERE pu.promoCode.promoId = :promoId AND pu.user.userId = :userId")
    long countByPromoIdAndUserId(@Param("promoId") Integer promoId, @Param("userId") Integer userId);

    long countByPromoCode(PromoCode promoCode);

    List<PromoUsage> findByPromoCodeAndUser(PromoCode promoCode, User user);
}
