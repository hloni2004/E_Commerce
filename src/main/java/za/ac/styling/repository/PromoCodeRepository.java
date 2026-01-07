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

    Optional<PromoCode> findByCode(String code);

    Optional<PromoCode> findByCodeIgnoreCase(String code);

    List<PromoCode> findByIsActiveTrue();

    @Query("SELECT p FROM PromoCode p WHERE p.isActive = true " +
           "AND (p.startDate IS NULL OR p.startDate <= :now) " +
           "AND (p.endDate IS NULL OR p.endDate >= :now) " +
           "AND (p.usageLimit IS NULL OR p.currentUsage < p.usageLimit)")
    List<PromoCode> findValidPromoCodes(@Param("now") LocalDateTime now);

    boolean existsByCodeIgnoreCase(String code);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE PromoCode p SET p.currentUsage = p.currentUsage + 1 WHERE p.promoId = :id AND (p.usageLimit IS NULL OR p.currentUsage < p.usageLimit)")
    int incrementUsageIfBelowLimit(@org.springframework.data.repository.query.Param("id") Integer id);
}
