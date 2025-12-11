package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PayPalPayment extends PaymentMethod {
    private String email;
    private String payerId;


    @Override
    public boolean validate() { return email.contains("@"); }
}