package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;

    private double amount;
    private String currency;
    private LocalDate paymentDate;

    @OneToOne(mappedBy = "payment", fetch = FetchType.LAZY)
    @JsonIgnore
    private Order order;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    private PaymentMethod paymentMethod;

    private String transactionId;
    private String failureReason;
}