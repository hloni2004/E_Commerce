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

    @ManyToOne
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductColour> colours;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    @OneToOne
    @JoinColumn(name = "primary_image_id")
    @JsonIgnore  // Prevents circular reference during JSON serialization
    private ProductImage primaryImage;

    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDate updatedAt;
}