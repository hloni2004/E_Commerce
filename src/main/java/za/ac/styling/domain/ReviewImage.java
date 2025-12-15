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

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imageData;

    private String contentType; // image/jpeg, image/png, etc.
}
