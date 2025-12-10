package za.ac.styling.service;

import za.ac.styling.domain.ShippingMethod;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for ShippingMethod entity
 */
public interface ShippingMethodService extends IService<ShippingMethod, Long> {

    /**
     * Find shipping method by name
     */
    Optional<ShippingMethod> findByName(String name);

    /**
     * Find all active shipping methods
     */
    List<ShippingMethod> findActiveShippingMethods();

    /**
     * Find all inactive shipping methods
     */
    List<ShippingMethod> findInactiveShippingMethods();

    /**
     * Find shipping methods by cost range
     */
    List<ShippingMethod> findByCostRange(double minCost, double maxCost);

    /**
     * Find active shipping methods ordered by cost
     */
    List<ShippingMethod> findActiveShippingMethodsOrderedByCost();

    /**
     * Check if shipping method name exists
     */
    boolean existsByName(String name);

    /**
     * Activate shipping method
     */
    ShippingMethod activateShippingMethod(Long methodId);

    /**
     * Deactivate shipping method
     */
    ShippingMethod deactivateShippingMethod(Long methodId);
}