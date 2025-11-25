package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;


    @ManyToOne
    private User user;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;


    private String orderNumber;
    private double totalAmount;
    private double subtotal;
    private double shippingCost;
    private double taxAmount;
    private double discountAmount;
    private Date orderDate;


    @OneToOne
    private Payment payment;


    @ManyToOne
    private ShippingMethod shippingMethod;


    @ManyToOne
    private Address shippingAddress;


    @ManyToOne
    private Address billingAddress;


    private boolean invoiceEmailSent;
    private String notes;


    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}