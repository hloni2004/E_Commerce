

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
     * Validate promo code for a user and cart
     * Returns validation result with error message if invalid
     */
    PromoValidationResult validatePromoCode(String code, Integer userId, List<Integer> productIds, double cartTotal);

    /**
     * Apply promo code to cart items and calculate discount
     * Returns discount details
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
}
