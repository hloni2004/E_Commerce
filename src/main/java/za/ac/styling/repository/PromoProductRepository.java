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

    List<PromoProduct> findByPromoCode(PromoCode promoCode);

    List<PromoProduct> findByPromoCode_PromoId(Integer promoId);

    boolean existsByPromoCodeAndProduct(PromoCode promoCode, Product product);

    @Query("SELECT CASE WHEN COUNT(pp) > 0 THEN true ELSE false END " +
           "FROM PromoProduct pp WHERE pp.promoCode.promoId = :promoId " +
           "AND pp.product.productId = :productId")
    boolean isProductEligible(@Param("promoId") Integer promoId, @Param("productId") Integer productId);

    void deleteByPromoCode_PromoId(Integer promoId);

    @Query("SELECT pp.product.productId FROM PromoProduct pp WHERE pp.promoCode.promoId = :promoId")
    List<Integer> findProductIdsByPromoId(@Param("promoId") Integer promoId);
}
