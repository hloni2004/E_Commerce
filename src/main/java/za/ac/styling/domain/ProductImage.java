package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.*;
import java.util.Base64;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"product"})
@ToString(exclude = {"product", "imageData"})
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
    @JsonIgnore  // Don't serialize blob data directly
    private byte[] imageData;
    
    @JsonGetter("imageData")
    public String getImageDataAsBase64() {
        if (imageData != null && imageData.length > 0) {
            return "data:" + (contentType != null ? contentType : "image/jpeg") + ";base64," + Base64.getEncoder().encodeToString(imageData);
        }
        return null;
    }
    
    private String imageUrl;  // Keep for backward compatibility
    private String contentType;  // e.g., "image/jpeg", "image/png"
    private String altText;
    private int displayOrder;
    private boolean isPrimary;
}
