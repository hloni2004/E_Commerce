package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @OneToOne
    @JoinColumn(name = "primary_image_id")
    private ProductImage primaryImage;




    private String imageUrl;
    private String altText;
    private int displayOrder;
    private boolean isPrimary;
}
