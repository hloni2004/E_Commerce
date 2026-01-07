package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "colour_id")
    private ProductColour colour;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "colour_size_id")
    private ProductColourSize colourSize;

    private int quantity;

    @Column(name = "price_at_purchase", nullable = false)
    private double price;

    @Column(name = "subtotal")
    private double subtotal;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;
}
