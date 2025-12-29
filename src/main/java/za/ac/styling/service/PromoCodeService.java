

package za.ac.styling.service;

import za.ac.styling.domain.PromoCode;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.User;

import java.util.List;
import java.util.Map;

/**
 * Service interface for PromoCode management
 */
public interface PromoCodeService extends IService<PromoCode, Integer> {


    /**
     * Find promo code by code string
     */
    PromoCode findByCode(String code);

    /**
     * Single authoritative promo processing method. Performs validation, eligibility checks, and discount calculation.
     * If `finalizeUsage` is true, this method will also attempt to record usage atomically (requires orderId to be set)
     * and will enforce usage limits as part of the same transactional operation.
     *
     * - `cartSubtotalCents` MUST be supplied as integer cents. The service is responsible for all money calculations.
     * - When `finalizeUsage` is false, the method only validates and returns a preview. (Recommended for checkout preview.)
     * - When `finalizeUsage` is true, the method will record promo usage (PromoUsage) and increment the usage counter atomically.
     */
    PromoApplicationResult processPromo(String code, Integer userId, Map<Integer, Integer> productQuantities,
                                         long cartSubtotalCents, boolean finalizeUsage, Integer orderId);

    /**
     * Backwards-compatible helpers which delegate to `processPromo` (kept for existing callers).
     */
    PromoValidationResult validatePromoCode(String code, Integer userId, List<Integer> productIds, double cartTotal);

    /**
     * Apply promo code to cart items and calculate discount (DELEGATES to `processPromo` using cents).
     * Returns discount details.
     */
    PromoDiscountResult applyPromoCode(String code, Integer userId, Map<Integer, Integer> productQuantities,
            double cartSubtotal);

    /**
     * Create a new promo code with eligible products
     */
    PromoCode createPromoWithProducts(PromoCode promoCode, List<Integer> productIds);

    /**
     * Update promo code and its eligible products
     */
    PromoCode updatePromoWithProducts(Integer promoId, PromoCode promoCode, List<Integer> productIds);

    /**
     * Track promo code usage when order is placed
     */
    void recordPromoUsage(Integer promoId, Integer userId, Integer orderId);

    /**
     * Check if user has already used a promo code
     */
    boolean hasUserUsedPromo(Integer promoId, Integer userId);

    /**
     * Get all active and valid promo codes
     */
    List<PromoCode> getValidPromoCodes();

    /**
     * Get eligible product IDs for a promo
     */
    List<Integer> getEligibleProductIds(Integer promoId);

    /**
     * Result class for promo validation
     */
    class PromoValidationResult {
        private boolean valid;
        private String message;
        private PromoCode promoCode;

        public PromoValidationResult(boolean valid, String message, PromoCode promoCode) {
            this.valid = valid;
            this.message = message;
            this.promoCode = promoCode;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public PromoCode getPromoCode() {
            return promoCode;
        }
    }

    /**
     * Result class for promo discount calculation
     */
    class PromoDiscountResult {
        private boolean applied;
        private double discountAmount;
        private double finalTotal;
        private String message;
        private List<Integer> eligibleProductIds;

        public PromoDiscountResult(boolean applied, double discountAmount, double finalTotal,
                String message, List<Integer> eligibleProductIds) {
            this.applied = applied;
            this.discountAmount = discountAmount;
            this.finalTotal = finalTotal;
            this.message = message;
            this.eligibleProductIds = eligibleProductIds;
        }

        public boolean isApplied() {
            return applied;
        }

        public double getDiscountAmount() {
            return discountAmount;
        }

        public double getFinalTotal() {
            return finalTotal;
        }

        public String getMessage() {
            return message;
        }

        public List<Integer> getEligibleProductIds() {
            return eligibleProductIds;
        }
    }

    /**
     * Unified application result used by `processPromo`.
     */
    class PromoApplicationResult {
        private boolean applied;
        private long discountAmountCents;
        private long finalTotalCents;
        private String message;
        private List<Integer> eligibleProductIds;
        private Integer promoId;
        private PromoCode promoCode;

        public PromoApplicationResult(boolean applied, long discountAmountCents, long finalTotalCents, String message,
                                       List<Integer> eligibleProductIds, Integer promoId, PromoCode promoCode) {
            this.applied = applied;
            this.discountAmountCents = discountAmountCents;
            this.finalTotalCents = finalTotalCents;
            this.message = message;
            this.eligibleProductIds = eligibleProductIds;
            this.promoId = promoId;
            this.promoCode = promoCode;
        }

        public boolean isApplied() { return applied; }
        public long getDiscountAmountCents() { return discountAmountCents; }
        public long getFinalTotalCents() { return finalTotalCents; }
        public String getMessage() { return message; }
        public List<Integer> getEligibleProductIds() { return eligibleProductIds; }
        public Integer getPromoId() { return promoId; }
        public PromoCode getPromoCode() { return promoCode; }

        public static PromoApplicationResult success(long discountAmountCents, long finalTotalCents, String message,
                                                     List<Integer> eligibleProductIds, Integer promoId, PromoCode promoCode) {
            return new PromoApplicationResult(true, discountAmountCents, finalTotalCents, message, eligibleProductIds, promoId, promoCode);
        }

        public static PromoApplicationResult failure(String message) {
            return new PromoApplicationResult(false, 0L, 0L, message, new java.util.ArrayList<>(), null, null);
        }
    }
}

