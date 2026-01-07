package za.ac.styling.service;

import za.ac.styling.domain.ShippingMethod;

import java.util.List;
import java.util.Optional;

public interface ShippingMethodService extends IService<ShippingMethod, Long> {

    Optional<ShippingMethod> findByName(String name);

    List<ShippingMethod> findActiveShippingMethods();

    List<ShippingMethod> findInactiveShippingMethods();

    List<ShippingMethod> findByCostRange(double minCost, double maxCost);

    List<ShippingMethod> findActiveShippingMethodsOrderedByCost();

    boolean existsByName(String name);

    ShippingMethod activateShippingMethod(Long methodId);

    ShippingMethod deactivateShippingMethod(Long methodId);
}