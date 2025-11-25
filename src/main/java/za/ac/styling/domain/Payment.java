package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;


    private double amount;
    private String currency;
    private LocalDate paymentDate;


    @Enumerated(EnumType.STRING)
    private PaymentStatus status;


    @OneToOne
    private PaymentMethod paymentMethod;


    private String transactionId;
    private String failureReason;
}