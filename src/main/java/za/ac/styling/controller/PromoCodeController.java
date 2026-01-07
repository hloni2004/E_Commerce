package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.domain.PromoCode;
import za.ac.styling.service.PromoCodeService;
import za.ac.styling.repository.ProductRepository;

import java.util.*;

@RestController
@RequestMapping("/api/promos")
public class PromoCodeController {

    @Autowired
    private PromoCodeService promoCodeService;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createPromo(@RequestBody PromoCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            PromoCode promoCode = PromoCode.builder()
                    .code(request.getCode().toUpperCase())
                    .discountType(request.getDiscountType())
                    .discountValue(request.getDiscountValue())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .usageLimit(request.getUsageLimit())
                    .perUserUsageLimit(request.getPerUserUsageLimit())
                    .currentUsage(0)
                    .minPurchaseAmount(request.getMinPurchaseAmount())
                    .isActive(request.isActive() != null ? request.isActive() : true)
                    .description(request.getDescription())
                    .build();

            PromoCode created = promoCodeService.createPromoWithProducts(promoCode, request.getProductIds());

            response.put("success", true);
            response.put("message", "Promo code created successfully");
            response.put("promo", created);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating promo code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllPromos() {
        try {
            List<PromoCode> promos = promoCodeService.getAll();

            List<Map<String, Object>> promosWithProducts = new ArrayList<>();
            for (PromoCode promo : promos) {
                Map<String, Object> promoData = new HashMap<>();
                promoData.put("promo", promo);
                promoData.put("eligibleProductIds", promoCodeService.getEligibleProductIds(promo.getPromoId()));
                promosWithProducts.add(promoData);
            }

            return ResponseEntity.ok(promosWithProducts);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching promo codes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPromo(@PathVariable Integer id) {
        try {
            PromoCode promo = promoCodeService.read(id);
            if (promo == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Promo code not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("promo", promo);
            response.put("eligibleProductIds", promoCodeService.getEligibleProductIds(id));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching promo code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePromo(@PathVariable Integer id, @RequestBody PromoCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            PromoCode promoCode = PromoCode.builder()
                    .code(request.getCode().toUpperCase())
                    .discountType(request.getDiscountType())
                    .discountValue(request.getDiscountValue())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .usageLimit(request.getUsageLimit())
                    .minPurchaseAmount(request.getMinPurchaseAmount())
                    .isActive(request.isActive() != null ? request.isActive() : true)
                    .description(request.getDescription())
                    .build();

            PromoCode updated = promoCodeService.updatePromoWithProducts(id, promoCode, request.getProductIds());

            response.put("success", true);
            response.put("message", "Promo code updated successfully");
            response.put("promo", updated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating promo code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePromo(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            promoCodeService.delete(id);
            response.put("success", true);
            response.put("message", "Promo code deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting promo code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validatePromo(@RequestBody PromoValidateRequest request) {
        try {

            long subtotalCents = 0L;
            Map<Integer, Integer> qtyMap = request.getProductQuantities() != null ? request.getProductQuantities()
                    : new HashMap<>();
            for (Map.Entry<Integer, Integer> e : qtyMap.entrySet()) {
                var pOpt = productRepository.findById(e.getKey());
                if (pOpt.isPresent()) {
                    subtotalCents += Math.round(pOpt.get().getBasePrice() * 100) * e.getValue();
                }
            }

            PromoCodeService.PromoApplicationResult result = promoCodeService.processPromo(
                    request.getCode(),
                    request.getUserId(),
                    qtyMap,
                    subtotalCents,
                    false,
                    null);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", result.isApplied());
            response.put("message", result.getMessage());
            if (result.isApplied() && result.getPromoCode() != null) {
                response.put("promo", result.getPromoCode());
                response.put("eligibleProductIds", result.getEligibleProductIds());
                response.put("discountAmount", result.getDiscountAmountCents() / 100.0);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Error validating promo code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyPromo(@RequestBody PromoApplyRequest request) {
        try {

            long subtotalCents = 0L;
            Map<Integer, Integer> qtyMap = request.getProductQuantities() != null ? request.getProductQuantities()
                    : new HashMap<>();
            for (Map.Entry<Integer, Integer> e : qtyMap.entrySet()) {
                var pOpt = productRepository.findById(e.getKey());
                if (pOpt.isPresent()) {
                    subtotalCents += Math.round(pOpt.get().getBasePrice() * 100) * e.getValue();
                }
            }

            PromoCodeService.PromoApplicationResult result = promoCodeService.processPromo(
                    request.getCode(),
                    request.getUserId(),
                    qtyMap,
                    subtotalCents,
                    false,
                    null);

            Map<String, Object> response = new HashMap<>();
            response.put("applied", result.isApplied());
            response.put("discountAmount", result.getDiscountAmountCents() / 100.0);
            response.put("finalTotal", result.getFinalTotalCents() / 100.0);
            response.put("message", result.getMessage());
            response.put("eligibleProductIds", result.getEligibleProductIds());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("applied", false);
            response.put("message", "Error applying promo code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public static class PromoCreateRequest {
        private String code;
        private PromoCode.DiscountType discountType;
        private double discountValue;
        private java.time.LocalDateTime startDate;
        private java.time.LocalDateTime endDate;
        private Integer usageLimit;
        private Integer perUserUsageLimit;
        private Double minPurchaseAmount;
        private Boolean isActive;
        private String description;
        private List<Integer> productIds;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public PromoCode.DiscountType getDiscountType() {
            return discountType;
        }

        public void setDiscountType(PromoCode.DiscountType discountType) {
            this.discountType = discountType;
        }

        public double getDiscountValue() {
            return discountValue;
        }

        public void setDiscountValue(double discountValue) {
            this.discountValue = discountValue;
        }

        public java.time.LocalDateTime getStartDate() {
            return startDate;
        }

        public void setStartDate(java.time.LocalDateTime startDate) {
            this.startDate = startDate;
        }

        public java.time.LocalDateTime getEndDate() {
            return endDate;
        }

        public void setEndDate(java.time.LocalDateTime endDate) {
            this.endDate = endDate;
        }

        public Integer getUsageLimit() {
            return usageLimit;
        }

        public void setUsageLimit(Integer usageLimit) {
            this.usageLimit = usageLimit;
        }

        public Integer getPerUserUsageLimit() {
            return perUserUsageLimit;
        }

        public void setPerUserUsageLimit(Integer perUserUsageLimit) {
            this.perUserUsageLimit = perUserUsageLimit;
        }

        public Double getMinPurchaseAmount() {
            return minPurchaseAmount;
        }

        public void setMinPurchaseAmount(Double minPurchaseAmount) {
            this.minPurchaseAmount = minPurchaseAmount;
        }

        public Boolean isActive() {
            return isActive;
        }

        public void setActive(Boolean active) {
            isActive = active;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<Integer> getProductIds() {
            return productIds;
        }

        public void setProductIds(List<Integer> productIds) {
            this.productIds = productIds;
        }
    }

    public static class PromoValidateRequest {
        private String code;
        private Integer userId;
        private Map<Integer, Integer> productQuantities;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public Map<Integer, Integer> getProductQuantities() {
            return productQuantities;
        }

        public void setProductQuantities(Map<Integer, Integer> productQuantities) {
            this.productQuantities = productQuantities;
        }
    }

    public static class PromoApplyRequest {
        private String code;
        private Integer userId;
        private Map<Integer, Integer> productQuantities;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public Map<Integer, Integer> getProductQuantities() {
            return productQuantities;
        }

        public void setProductQuantities(Map<Integer, Integer> productQuantities) {
            this.productQuantities = productQuantities;
        }
    }
}
