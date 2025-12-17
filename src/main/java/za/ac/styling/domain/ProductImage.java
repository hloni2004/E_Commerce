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
@EqualsAndHashCode(exclude = {"product"})
@ToString(exclude = {"product"})
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore  // Prevent circular reference
    private Product product;

    // SUPABASE STORAGE FIELDS
    @Column(length = 500)
    private String supabaseUrl;  // Full public URL to image in Supabase
    
    @Column(length = 300)
    private String bucketPath;   // Path in Supabase bucket (for deletion)
    
    // LEGACY BLOB SUPPORT (for backward compatibility during migration)
    @Lob
    @JsonIgnore
    private byte[] imageData;    // Will be phased out - PostgreSQL uses BYTEA
    
    // COMMON FIELDS
    private String imageUrl;     // Computed property: returns supabaseUrl or legacy URL
    private String contentType;  // e.g., "image/jpeg", "image/png"
    private String altText;
    private int displayOrder;
    private boolean isPrimary;
    
    /**
     * Get the image URL (prioritizes Supabase URL)
     */
    public String getImageUrl() {
        return supabaseUrl != null ? supabaseUrl : imageUrl;
    }
    
    /**
     * Check if this image is stored in Supabase
     */
    public boolean isSupabaseImage() {
        return supabaseUrl != null && !supabaseUrl.isEmpty();
    }
    
    /**
     * Check if this image is stored as BLOB (legacy)
     */
    public boolean isBlobImage() {
        return imageData != null && imageData.length > 0;
    }
}
