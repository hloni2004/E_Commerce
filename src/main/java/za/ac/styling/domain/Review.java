package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"images"})
@ToString(exclude = {"images"})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne
    private User user;

    @ManyToOne
    @JsonIgnore
    private Product product;

    private Integer productId; // For easier JSON serialization

    private int rating; // 1-5 stars
    
    @Column(length = 1000)
    private String comment;
    
    private Date reviewDate;
    
    private boolean verified; // True if user purchased the product
    
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images;
    
    // Helpful votes
    private int helpfulCount;
}