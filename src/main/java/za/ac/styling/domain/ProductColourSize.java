package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductColourSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sizeId;


    private String sizeName;
    private int stockQuantity;
    private int reservedQuantity;
    private int reorderLevel;


    @ManyToOne
    private ProductColour colour;
}