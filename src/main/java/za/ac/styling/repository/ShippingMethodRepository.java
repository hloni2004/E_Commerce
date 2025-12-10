package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.ShippingMethod;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ShippingMethod entity
 */
@Repository
public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long> {

    /**
     * Find shipping method by name
     */
    Optional<ShippingMethod> findByName(String name);

    /**
     * Find all active shipping methods
     */
    List<ShippingMethod> findByIsActiveTrue();

    /**
     * Find all inactive shipping methods
     */
    List<ShippingMethod> findByIsActiveFalse();

    /**
     * Find shipping methods by cost range
     */
    List<ShippingMethod> findByCostBetween(double minCost, double maxCost);

    /**
     * Find active shipping methods ordered by cost
     */
    List<ShippingMethod> findByIsActiveTrueOrderByCostAsc();

    /**
     * Check if shipping method name exists
     */
    boolean existsByName(String name);
}
