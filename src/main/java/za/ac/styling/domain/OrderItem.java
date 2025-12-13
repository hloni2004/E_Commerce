package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

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
    @JsonBackReference
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

    @Column(name = "price_at_purchase", nullable = false)
    private double price; // Price per unit at time of purchase

    @Column(name = "subtotal")
    private double subtotal; // quantity * price

    @Column(name = "total_price", nullable = false)
    private double totalPrice; // Total price for this item (same as subtotal)
}
