package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
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
    private Product product;


    @OneToMany(mappedBy = "colour", cascade = CascadeType.ALL)
    private List<ProductColourSize> sizes;
}