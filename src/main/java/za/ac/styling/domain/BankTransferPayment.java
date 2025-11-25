package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BankTransferPayment extends PaymentMethod {
    private String bankName;
    private String accountNumber;
    private String routingNumber;


    @Override
    public boolean validate() { return accountNumber != null; }
}
