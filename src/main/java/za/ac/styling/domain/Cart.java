package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;


    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;



    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartItem> items;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}