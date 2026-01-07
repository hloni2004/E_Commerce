package za.ac.styling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.styling.domain.ShippingMethod;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long> {

    Optional<ShippingMethod> findByName(String name);

    List<ShippingMethod> findByIsActiveTrue();

    List<ShippingMethod> findByIsActiveFalse();

    List<ShippingMethod> findByCostBetween(double minCost, double maxCost);

    List<ShippingMethod> findByIsActiveTrueOrderByCostAsc();

    boolean existsByName(String name);
}
