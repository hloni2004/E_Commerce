
package za.ac.styling.service;

import za.ac.styling.domain.PromoCode;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.User;

import java.util.List;
import java.util.Map;

public interface PromoCodeService extends IService<PromoCode, Integer> {

    PromoCode findByCode(String code);

    PromoApplicationResult processPromo(String code, Integer userId, Map<Integer, Integer> productQuantities,
                                         long cartSubtotalCents, boolean finalizeUsage, Integer orderId);

    PromoValidationResult validatePromoCode(String code, Integer userId, List<Integer> productIds, double cartTotal);

    PromoDiscountResult applyPromoCode(String code, Integer userId, Map<Integer, Integer> productQuantities,
            double cartSubtotal);

    PromoCode createPromoWithProducts(PromoCode promoCode, List<Integer> productIds);

    PromoCode updatePromoWithProducts(Integer promoId, PromoCode promoCode, List<Integer> productIds);

    void recordPromoUsage(Integer promoId, Integer userId, Integer orderId);

    boolean hasUserUsedPromo(Integer promoId, Integer userId);

    List<PromoCode> getValidPromoCodes();

    List<Integer> getEligibleProductIds(Integer promoId);

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

