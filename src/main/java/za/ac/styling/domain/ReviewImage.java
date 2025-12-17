package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne
    @JsonIgnore
    private Review review;

    // SUPABASE STORAGE FIELDS
    @Column(length = 500)
    private String supabaseUrl;  // Full public URL to image in Supabase
    
    @Column(length = 300)
    private String bucketPath;   // Path in Supabase bucket (for deletion)
    
    // LEGACY BLOB SUPPORT (for backward compatibility during migration)
    @Lob
    private byte[] imageData;    // Will be phased out - PostgreSQL uses BYTEA

    private String contentType;  // image/jpeg, image/png, etc.
    
    /**
     * Get the image URL (prioritizes Supabase URL)
     */
    public String getImageUrl() {
        return supabaseUrl != null ? supabaseUrl : null;
    }
    
    /**
     * Check if this image is stored in Supabase
     */
    public boolean isSupabaseImage() {
        return supabaseUrl != null && !supabaseUrl.isEmpty();
    }
}
