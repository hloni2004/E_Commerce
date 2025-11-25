package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayPalPayment extends PaymentMethod {
    private String email;
    private String payerId;


    @Override
    public boolean validate() { return email.contains("@"); }
}