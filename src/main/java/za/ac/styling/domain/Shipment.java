package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipmentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private String trackingNumber;
    private String carrier;

    @ManyToOne(fetch = FetchType.LAZY)
    private ShippingMethod shippingMethod;

    private LocalDateTime shippedDate;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;
}