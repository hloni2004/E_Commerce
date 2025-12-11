package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore  // Prevent circular reference
    private Product product;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore  // Don't serialize blob data in JSON responses
    private byte[] imageData;
    
    private String imageUrl;  // Keep for backward compatibility
    private String contentType;  // e.g., "image/jpeg", "image/png"
    private String altText;
    private int displayOrder;
    private boolean isPrimary;
}
