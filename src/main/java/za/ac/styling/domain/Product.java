package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "colours", "images", "primaryImage", "category" })
@ToString(exclude = { "colours", "images", "primaryImage" })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    private String name;
    private String description;
    private double basePrice;
    private double comparePrice;
    private String sku;
    private double weight;
    private int reorderLevel; // Stock level that triggers low inventory notification

    @ManyToOne(fetch = FetchType.EAGER)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ProductColour> colours;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ProductImage> images;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "primary_image_id", nullable = true)
    private ProductImage primaryImage;

    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDate updatedAt;
}