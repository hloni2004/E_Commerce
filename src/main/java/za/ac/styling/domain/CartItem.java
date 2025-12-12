package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartItemId;


    @ManyToOne
    @JsonBackReference
    private Cart cart;


    @ManyToOne
    private Product product;


    @ManyToOne
    private ProductColour colour;


    @ManyToOne
    private ProductColourSize size;


    private int quantity;
}
