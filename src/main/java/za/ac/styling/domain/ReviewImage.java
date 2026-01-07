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

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Review review;

    @Column(length = 500)
    private String supabaseUrl;

    @Column(length = 300)
    private String bucketPath;

    private String contentType;

    public String getImageUrl() {
        return supabaseUrl != null ? supabaseUrl : null;
    }

    public boolean isSupabaseImage() {
        return supabaseUrl != null && !supabaseUrl.isEmpty();
    }
}
