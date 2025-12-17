package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "promo_codes")
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer promoId;

    @Column(unique = true, nullable = false)
    private String code; // Unique promo code (e.g., "SUMMER20")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // PERCENTAGE or FIXED

    @Column(nullable = false)
    private double discountValue; // Percentage (e.g., 20) or fixed amount (e.g., 100.00)

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer usageLimit; // Max number of times the promo can be used (null = unlimited)
    
    @Builder.Default
    @Column(nullable = false)
    private Integer currentUsage = 0; // Track how many times it has been used

    private Double minPurchaseAmount; // Minimum cart total required (null = no minimum)

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true; // Admin can enable/disable

    private String description; // Optional description for admin reference

    @OneToMany(mappedBy = "promoCode", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PromoProduct> promoProducts; // Products eligible for this promo

    @OneToMany(mappedBy = "promoCode", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PromoUsage> promoUsages; // Track user usage

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to check if promo is currently valid
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive 
            && (startDate == null || !now.isBefore(startDate))
            && (endDate == null || !now.isAfter(endDate))
            && (usageLimit == null || currentUsage < usageLimit);
    }

    // Discount type enum
    public enum DiscountType {
        PERCENTAGE,
        FIXED
    }
}
