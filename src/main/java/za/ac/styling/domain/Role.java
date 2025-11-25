package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;


// ================= ROLE ==================
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;


    private String roleName;


    @ManyToMany(fetch = FetchType.EAGER)
    private List<Permission> permissions;
}