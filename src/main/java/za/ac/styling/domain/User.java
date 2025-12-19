package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@EqualsAndHashCode(exclude = {"cart", "addresses", "paymentMethods"})
@ToString(exclude = {"cart", "addresses", "paymentMethods"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;


    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;


    @ManyToOne
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Address> addresses;


    private boolean isActive;
    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PaymentMethod> paymentMethods;

}