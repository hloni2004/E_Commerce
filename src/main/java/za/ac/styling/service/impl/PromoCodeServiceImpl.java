package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import za.ac.styling.domain.*;
import za.ac.styling.repository.*;
import za.ac.styling.service.PromoCodeService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for PromoCode management
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PromoCodeServiceImpl implements PromoCodeService {

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Autowired
    private PromoProductRepository promoProductRepository;

    @Autowired
    private PromoUsageRepository promoUsageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public PromoCode create(PromoCode promoCode) {
        if (promoCodeRepository.existsByCodeIgnoreCase(promoCode.getCode())) {
            throw new IllegalArgumentException("Promo code already exists: " + promoCode.getCode());
        }
        return promoCodeRepository.save(promoCode);
    }

    @Override
    public PromoCode read(Integer id) {
        return promoCodeRepository.findById(id).orElse(null);
    }

    @Override
    public PromoCode update(PromoCode promoCode) {
        return promoCodeRepository.save(promoCode);
    }

    @Override
    public List<PromoCode> getAll() {
        return promoCodeRepository.findAll();
    }

    @Override
    public void delete(Integer id) {
        promoCodeRepository.deleteById(id);
    }

    @Override
    public PromoCode findByCode(String code) {
        return promoCodeRepository.findByCodeIgnoreCase(code).orElse(null);
    }

    @Override
    @Transactional
    public PromoCode createPromoWithProducts(PromoCode promoCode, List<Integer> productIds) {
        // Validate code uniqueness
        if (promoCodeRepository.existsByCodeIgnoreCase(promoCode.getCode())) {
            throw new IllegalArgumentException("Promo code already exists: " + promoCode.getCode());
        }

        // Save promo code
        PromoCode savedPromo = promoCodeRepository.save(promoCode);

        // Add eligible products
        if (productIds != null && !productIds.isEmpty()) {
            for (Integer productId : productIds) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

                PromoProduct promoProduct = PromoProduct.builder()
                        .promoCode(savedPromo)
                        .product(product)
                        .build();
                promoProductRepository.save(promoProduct);
            }
        }

        return savedPromo;
    }

    @Override
    @Transactional
    public PromoCode updatePromoWithProducts(Integer promoId, PromoCode promoCode, List<Integer> productIds) {
        PromoCode existingPromo = promoCodeRepository.findById(promoId)
                .orElseThrow(() -> new IllegalArgumentException("Promo code not found: " + promoId));

        // Check if code is being changed to an existing code
        if (!existingPromo.getCode().equalsIgnoreCase(promoCode.getCode()) &&
                promoCodeRepository.existsByCodeIgnoreCase(promoCode.getCode())) {
            throw new IllegalArgumentException("Promo code already exists: " + promoCode.getCode());
        }

        // Update promo fields
        existingPromo.setCode(promoCode.getCode());
        existingPromo.setDiscountType(promoCode.getDiscountType());
        existingPromo.setDiscountValue(promoCode.getDiscountValue());
        existingPromo.setStartDate(promoCode.getStartDate());
        existingPromo.setEndDate(promoCode.getEndDate());
        existingPromo.setUsageLimit(promoCode.getUsageLimit());
        existingPromo.setMinPurchaseAmount(promoCode.getMinPurchaseAmount());
        existingPromo.setActive(promoCode.isActive());
        existingPromo.setDescription(promoCode.getDescription());

        PromoCode updatedPromo = promoCodeRepository.save(existingPromo);

        // Update eligible products
        promoProductRepository.deleteByPromoCode_PromoId(promoId);
        if (productIds != null && !productIds.isEmpty()) {
            for (Integer productId : productIds) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

                PromoProduct promoProduct = PromoProduct.builder()
                        .promoCode(updatedPromo)
                        .product(product)
                        .build();
                promoProductRepository.save(promoProduct);
            }
        }

        return updatedPromo;
    }

    @Override
    public PromoValidationResult validatePromoCode(String code, Integer userId, List<Integer> productIds,
            double cartTotal) {
        // Find promo code
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code).orElse(null);
        if (promoCode == null) {
            return new PromoValidationResult(false, "Promo code not found", null);
        }

        // Check if active
        if (!promoCode.isActive()) {
            return new PromoValidationResult(false, "Promo code is inactive", promoCode);
        }

        // Check date validity
        LocalDateTime now = LocalDateTime.now();
        if (promoCode.getStartDate() != null && now.isBefore(promoCode.getStartDate())) {
            return new PromoValidationResult(false, "Promo code not yet valid", promoCode);
        }
        if (promoCode.getEndDate() != null && now.isAfter(promoCode.getEndDate())) {
            return new PromoValidationResult(false, "Promo code has expired", promoCode);
        }

        // Check usage limit
        if (promoCode.getUsageLimit() != null && promoCode.getCurrentUsage() >= promoCode.getUsageLimit()) {
            return new PromoValidationResult(false, "Promo code usage limit reached", promoCode);
        }

        // Check if user already used (one-time per user)
        if (userId != null && hasUserUsedPromo(promoCode.getPromoId(), userId)) {
            return new PromoValidationResult(false, "You have already used this promo code", promoCode);
        }

        // Check minimum purchase amount
        if (promoCode.getMinPurchaseAmount() != null && cartTotal < promoCode.getMinPurchaseAmount()) {
            return new PromoValidationResult(false,
                    String.format("Minimum purchase of R%.2f required", promoCode.getMinPurchaseAmount()),
                    promoCode);
        }

        // Check if any products in cart are eligible
        List<Integer> eligibleProductIds = getEligibleProductIds(promoCode.getPromoId());
        if (!eligibleProductIds.isEmpty()) {
            boolean hasEligibleProduct = productIds.stream()
                    .anyMatch(eligibleProductIds::contains);
            if (!hasEligibleProduct) {
                return new PromoValidationResult(false, "No eligible products in cart", promoCode);
            }
        }

        return new PromoValidationResult(true, "Promo code is valid", promoCode);
    }

    @Override
    public PromoDiscountResult applyPromoCode(String code, Integer userId, Map<Integer, Integer> productQuantities,
            double cartSubtotal) {
        // Validate first
        List<Integer> productIds = new ArrayList<>(productQuantities.keySet());
        PromoValidationResult validation = validatePromoCode(code, userId, productIds, cartSubtotal);

        if (!validation.isValid()) {
            return new PromoDiscountResult(false, 0, cartSubtotal, validation.getMessage(), new ArrayList<>());
        }

        PromoCode promoCode = validation.getPromoCode();
        List<Integer> eligibleProductIds = getEligibleProductIds(promoCode.getPromoId());

        // Calculate discount
        double discountAmount = 0;
        double eligibleTotal = 0;

        // If no specific products, apply to entire cart
        if (eligibleProductIds.isEmpty()) {
            eligibleTotal = cartSubtotal;
        } else {
            // Calculate total of eligible products only
            for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
                Integer productId = entry.getKey();
                if (eligibleProductIds.contains(productId)) {
                    Product product = productRepository.findById(productId).orElse(null);
                    if (product != null) {
                        eligibleTotal += product.getBasePrice() * entry.getValue();
                    }
                }
            }
        }

        // Apply discount based on type
        if (promoCode.getDiscountType() == PromoCode.DiscountType.PERCENTAGE) {
            discountAmount = eligibleTotal * (promoCode.getDiscountValue() / 100.0);
        } else if (promoCode.getDiscountType() == PromoCode.DiscountType.FIXED) {
            discountAmount = Math.min(promoCode.getDiscountValue(), eligibleTotal);
        }

        double finalTotal = cartSubtotal - discountAmount;

        String message = String.format("Promo code applied! Saved R%.2f", discountAmount);
        return new PromoDiscountResult(true, discountAmount, finalTotal, message, eligibleProductIds);
    }

    @Override
    @Transactional
    public void recordPromoUsage(Integer promoId, Integer userId, Integer orderId) {
        PromoCode promoCode = promoCodeRepository.findById(promoId)
                .orElseThrow(() -> new IllegalArgumentException("Promo code not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = null;
        if (orderId != null) {
            // Optional: fetch order if needed
        }

        // Create usage record
        PromoUsage usage = PromoUsage.builder()
                .promoCode(promoCode)
                .user(user)
                .order(order)
                .build();
        promoUsageRepository.save(usage);

        // Increment usage count
        promoCode.setCurrentUsage(promoCode.getCurrentUsage() + 1);
        promoCodeRepository.save(promoCode);
    }

    @Override
    public boolean hasUserUsedPromo(Integer promoId, Integer userId) {
        return promoUsageRepository.countByPromoIdAndUserId(promoId, userId) > 0;
    }

    @Override
    public List<PromoCode> getValidPromoCodes() {
        return promoCodeRepository.findValidPromoCodes(LocalDateTime.now());
    }

    @Override
    public List<Integer> getEligibleProductIds(Integer promoId) {
        return promoProductRepository.findProductIdsByPromoId(promoId);
    }
}
