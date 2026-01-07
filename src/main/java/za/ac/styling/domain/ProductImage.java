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
    @JsonIgnore
    private Product product;

    @Column(length = 500)
    private String supabaseUrl;

    @Column(length = 300)
    private String bucketPath;

    private String imageUrl;
    private String contentType;
    private String altText;
    private int displayOrder;
    private boolean isPrimary;

    public String getImageUrl() {
        return supabaseUrl != null ? supabaseUrl : imageUrl;
    }

    public boolean isSupabaseImage() {
        return supabaseUrl != null && !supabaseUrl.isEmpty();
    }
}
