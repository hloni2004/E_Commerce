package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductColour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer colourId;

    private String name;
    private String hexCode;

    @ManyToOne
    @JsonIgnore  // Prevent circular reference
    private Product product;

    @OneToMany(mappedBy = "colour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductColourSize> sizes;
}