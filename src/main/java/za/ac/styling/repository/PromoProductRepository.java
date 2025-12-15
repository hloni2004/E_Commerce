package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.PromoCode;
import za.ac.styling.domain.PromoProduct;
import za.ac.styling.domain.Product;

import java.util.List;

@Repository
public interface PromoProductRepository extends JpaRepository<PromoProduct, Integer> {

    /**
     * Find all products eligible for a specific promo code
     */
    List<PromoProduct> findByPromoCode(PromoCode promoCode);

    /**
     * Find all promo products by promo ID
     */
    List<PromoProduct> findByPromoCode_PromoId(Integer promoId);

    /**
     * Check if a specific product is eligible for a promo code
     */
    boolean existsByPromoCodeAndProduct(PromoCode promoCode, Product product);

    /**
     * Check if a promo applies to a specific product ID
     */
    @Query("SELECT CASE WHEN COUNT(pp) > 0 THEN true ELSE false END " +
           "FROM PromoProduct pp WHERE pp.promoCode.promoId = :promoId " +
           "AND pp.product.productId = :productId")
    boolean isProductEligible(@Param("promoId") Integer promoId, @Param("productId") Integer productId);

    /**
     * Delete all promo products for a specific promo code
     */
    void deleteByPromoCode_PromoId(Integer promoId);

    /**
     * Get all product IDs for a promo code
     */
    @Query("SELECT pp.product.productId FROM PromoProduct pp WHERE pp.promoCode.promoId = :promoId")
    List<Integer> findProductIdsByPromoId(@Param("promoId") Integer promoId);
}
