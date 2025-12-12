package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"user", "items"})
@ToString(exclude = {"user", "items"})
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;


    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;



    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL ,  orphanRemoval = true)
    @JsonManagedReference
    private List<CartItem> items;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}