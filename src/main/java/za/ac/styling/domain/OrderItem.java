package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "colour_id")
    private ProductColour colour;

    @ManyToOne
    @JoinColumn(name = "colour_size_id")
    private ProductColourSize colourSize;

    private int quantity;
    private double price; // Price per unit at time of purchase
    private double subtotal; // quantity * price
}
