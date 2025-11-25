package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;


    @OneToOne
    private ProductColourSize productColourSize;


    private int quantity;
    private int reservedQuantity;
    private LocalDateTime lastRestocked;
}