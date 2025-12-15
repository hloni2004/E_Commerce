package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "promo_products")
public class PromoProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "promo_id", nullable = false)
    private PromoCode promoCode;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Optional: Could add category-based promos in the future
    // @ManyToOne
    // @JoinColumn(name = "category_id")
    // private Category category;
}
