package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "colours", "images", "primaryImage", "category" })
@ToString(exclude = { "colours", "images", "primaryImage" })
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

    @ManyToOne
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductColour> colours;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    @OneToOne
    @JoinColumn(name = "primary_image_id")
    private ProductImage primaryImage;

    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDate updatedAt;
}