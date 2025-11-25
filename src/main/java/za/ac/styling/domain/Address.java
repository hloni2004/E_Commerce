package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;


    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;


    @Enumerated(EnumType.STRING)
    private AddressType addressType;


    private boolean isDefault;


    @ManyToOne
    private User user;
}
