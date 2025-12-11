package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.ShippingMethod;
import za.ac.styling.repository.ShippingMethodRepository;
import za.ac.styling.service.ShippingMethodService;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for ShippingMethod entity
 */
@Service
public class ShippingMethodServiceImpl implements ShippingMethodService {

    private ShippingMethodRepository shippingMethodRepository;

    @Autowired
    public ShippingMethodServiceImpl(ShippingMethodRepository shippingMethodRepository) {
        this.shippingMethodRepository = shippingMethodRepository;
    }

    @Override
    public ShippingMethod create(ShippingMethod shippingMethod) {
        return shippingMethodRepository.save(shippingMethod);
    }

    @Override
    public ShippingMethod read(Long id) {
        return shippingMethodRepository.findById(id).orElse(null);
    }

    @Override
    public ShippingMethod update(ShippingMethod shippingMethod) {
        return shippingMethodRepository.save(shippingMethod);
    }

    @Override
    public List<ShippingMethod> getAll() {
        return shippingMethodRepository.findAll();
    }

    @Override
    public Optional<ShippingMethod> findByName(String name) {
        return shippingMethodRepository.findByName(name);
    }

    @Override
    public List<ShippingMethod> findActiveShippingMethods() {
        return shippingMethodRepository.findByIsActiveTrue();
    }

    @Override
    public List<ShippingMethod> findInactiveShippingMethods() {
        return shippingMethodRepository.findByIsActiveFalse();
    }

    @Override
    public List<ShippingMethod> findByCostRange(double minCost, double maxCost) {
        return shippingMethodRepository.findByCostBetween(minCost, maxCost);
    }

    @Override
    public List<ShippingMethod> findActiveShippingMethodsOrderedByCost() {
        return shippingMethodRepository.findByIsActiveTrueOrderByCostAsc();
    }

    @Override
    public boolean existsByName(String name) {
        return shippingMethodRepository.existsByName(name);
    }

    @Override
    public ShippingMethod activateShippingMethod(Long methodId) {
        ShippingMethod method = read(methodId);
        if (method != null) {
            method.setActive(true);
            return update(method);
        }
        return null;
    }

    @Override
    public ShippingMethod deactivateShippingMethod(Long methodId) {
        ShippingMethod method = read(methodId);
        if (method != null) {
            method.setActive(false);
            return update(method);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        shippingMethodRepository.deleteById(id);
    }
}