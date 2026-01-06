package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderItem> items;

    private String orderNumber;
    private double totalAmount;
    private double subtotal;
    private double shippingCost;
    private double taxAmount;
    private double discountAmount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;
    private Date orderDate;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.EAGER)
    private ShippingMethod shippingMethod;

    @ManyToOne(fetch = FetchType.EAGER)
    private Address shippingAddress;

    @ManyToOne(fetch = FetchType.EAGER)
    private Address billingAddress;

    private boolean invoiceEmailSent;
    private String notes;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public PromoCode getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(PromoCode promoCode) {
        this.promoCode = promoCode;
    }
}