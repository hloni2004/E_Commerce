package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderItemId;


    @ManyToOne
    private Order order;


    @ManyToOne
    private Product product;


    @ManyToOne
    private ProductColour colour;


    @ManyToOne
    private ProductColourSize size;


    private int quantity;
    private double priceAtPurchase;
    private double unitPrice;
    private double totalPrice;
}
