package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "product" })
@ToString(exclude = { "product" })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // Prevent circular reference
    private Product product;

    // SUPABASE STORAGE FIELDS
    @Column(length = 500)
    private String supabaseUrl; // Full public URL to image in Supabase

    @Column(length = 300)
    private String bucketPath; // Path in Supabase bucket (for deletion)

    // COMMON FIELDS
    private String imageUrl; // Computed property: returns supabaseUrl or legacy URL
    private String contentType; // e.g., "image/jpeg", "image/png"
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
}
