package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long methodId;


    private String name;
    private String description;
    private double cost;
    private int estimatedDays;
    private boolean isActive;
}
