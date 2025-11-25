package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne
    private Product product;



    private String imageUrl;
    private String altText;
    private int displayOrder;
    private boolean isPrimary;
}
