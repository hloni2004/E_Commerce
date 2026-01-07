package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer promoId;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private double discountValue;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer usageLimit;

    @Builder.Default
    @Column(nullable = false)
    private Integer currentUsage = 0;

    private Double minPurchaseAmount;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean oneTimeUse = false;

    private Integer perUserUsageLimit;

    private String description;

    @OneToMany(mappedBy = "promoCode", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PromoProduct> promoProducts;

    @OneToMany(mappedBy = "promoCode", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PromoUsage> promoUsages;

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

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive
                && (startDate == null || !now.isBefore(startDate))
                && (endDate == null || !now.isAfter(endDate))
                && (usageLimit == null || currentUsage < usageLimit);
    }

    public enum DiscountType {
        PERCENTAGE,
        FIXED
    }
}
