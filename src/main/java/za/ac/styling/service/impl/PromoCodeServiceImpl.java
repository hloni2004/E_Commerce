package za.ac.styling.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PromoCodeServiceImpl.class);

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
        // Delegate to processPromo with cents and no finalization
        long cartCents = Math.round(cartTotal * 100);
        PromoCodeService.PromoApplicationResult result = processPromo(code, userId,
                productIds.stream().collect(Collectors.toMap(id -> id, id -> 1)), cartCents, false, null);
        if (!result.isApplied()) {
            return new PromoValidationResult(false, result.getMessage(), null);
        }
        return new PromoValidationResult(true, "Promo code is valid",
                promoCodeRepository.findByCodeIgnoreCase(code).orElse(null));
    }

    @Override
    public PromoDiscountResult applyPromoCode(String code, Integer userId, Map<Integer, Integer> productQuantities,
            double cartSubtotal) {
        // Delegate to processPromo (no finalization)
        long cartCents = Math.round(cartSubtotal * 100);
        PromoCodeService.PromoApplicationResult result = processPromo(code, userId, productQuantities, cartCents, false,
                null);
        if (!result.isApplied()) {
            return new PromoDiscountResult(false, 0, cartSubtotal, result.getMessage(), new ArrayList<>());
        }
        double discountAmount = result.getDiscountAmountCents() / 100.0;
        double finalTotal = result.getFinalTotalCents() / 100.0;
        return new PromoDiscountResult(true, discountAmount, finalTotal, result.getMessage(),
                result.getEligibleProductIds());
    }

    @Override
    @Transactional
    public PromoCodeService.PromoApplicationResult processPromo(String code, Integer userId,
            Map<Integer, Integer> productQuantities,
            long cartSubtotalCents, boolean finalizeUsage, Integer orderId) {
        log.info("Process promo: code={}, userId={}, products={}, cartCents={}, finalizeUsage={}", code, userId,
                productQuantities, cartSubtotalCents, finalizeUsage);

        // Find promo
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code).orElse(null);
        if (promoCode == null) {
            log.info("Promo not found: {}", code);
            return PromoApplicationResult.failure("Promo code not found");
        }

        // Check active and dates
        if (!promoCode.isActive()) {
            return PromoApplicationResult.failure("Promo code is inactive");
        }
        LocalDateTime now = LocalDateTime.now();
        if (promoCode.getStartDate() != null && now.isBefore(promoCode.getStartDate())) {
            return PromoApplicationResult.failure("Promo code not yet valid");
        }
        if (promoCode.getEndDate() != null && now.isAfter(promoCode.getEndDate())) {
            return PromoApplicationResult.failure("Promo code has expired");
        }

        // Usage limit check
        if (promoCode.getUsageLimit() != null && promoCode.getCurrentUsage() >= promoCode.getUsageLimit()) {
            return PromoApplicationResult.failure("Promo code usage limit reached");
        }

        // One-time per user check (if configured)
        if (promoCode.isOneTimeUse() && userId != null && hasUserUsedPromo(promoCode.getPromoId(), userId)) {
            return PromoApplicationResult.failure("You have already used this promo code");
        }

        // Min purchase amount check
        if (promoCode.getMinPurchaseAmount() != null) {
            long minCents = Math.round(promoCode.getMinPurchaseAmount() * 100);
            if (cartSubtotalCents < minCents) {
                return PromoApplicationResult
                        .failure(String.format("Minimum purchase of R%.2f required", promoCode.getMinPurchaseAmount()));
            }
        }

        // Determine eligible items total (in cents)
        List<Integer> eligibleProductIds = getEligibleProductIds(promoCode.getPromoId());
        long eligibleTotalCents = 0L;
        if (eligibleProductIds.isEmpty()) {
            eligibleTotalCents = cartSubtotalCents;
        } else {
            for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
                Integer productId = entry.getKey();
                if (eligibleProductIds.contains(productId)) {
                    Product product = productRepository.findById(productId).orElse(null);
                    if (product != null) {
                        long priceCents = Math.round(product.getBasePrice() * 100);
                        eligibleTotalCents += priceCents * entry.getValue();
                    }
                }
            }
            if (eligibleTotalCents == 0L) {
                return PromoApplicationResult.failure("No eligible products in cart");
            }
        }

        // Calculate discount in cents
        long discountCents = 0L;
        if (promoCode.getDiscountType() == PromoCode.DiscountType.PERCENTAGE) {
            // discountValue is percent (e.g. 20.0 for 20%)
            discountCents = Math.round((eligibleTotalCents * promoCode.getDiscountValue()) / 100.0);
        } else if (promoCode.getDiscountType() == PromoCode.DiscountType.FIXED) {
            long fixedCents = Math.round(promoCode.getDiscountValue() * 100);
            discountCents = Math.min(fixedCents, eligibleTotalCents);
        }

        long finalTotalCents = cartSubtotalCents - discountCents;
        String message = String.format("Promo applied! Saved R%.2f", discountCents / 100.0);

        // If finalizing usage, attempt atomic increment and record usage (must be in
        // same transaction)
        if (finalizeUsage) {
            // Re-check limit atomically
            int updated = promoCodeRepository.incrementUsageIfBelowLimit(promoCode.getPromoId());
            if (updated == 0) {
                return PromoApplicationResult.failure("Promo code usage limit reached");
            }

            // Record PromoUsage
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
            }
            za.ac.styling.domain.Order order = null;
            if (orderId != null) {
                order = null; // we don't need to hydrate the order now; storing order id may suffice in
                              // entity mapping
            }

            PromoUsage usage = PromoUsage.builder()
                    .promoCode(promoCode)
                    .user(user)
                    .order(order)
                    .build();
            promoUsageRepository.save(usage);

            // Refresh promoCode's usage count from DB (optional)
            promoCode = promoCodeRepository.findById(promoCode.getPromoId()).orElse(promoCode);
        }

        return PromoApplicationResult.success(discountCents, finalTotalCents, message, eligibleProductIds,
                promoCode.getPromoId(), promoCode);
    }

    @Override
    @Deprecated // Prefer `processPromo(..., finalizeUsage=true, orderId)` to ensure atomic
                // recording during order placement
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
