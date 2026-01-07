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
public class CardPayment extends PaymentMethod {
    private String cardNumber;
    private String cardHolder;
    private String expiryDate;
    private String cvv;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Override
    public boolean validate() { return cardNumber != null && cardNumber.length() >= 12; }
}
