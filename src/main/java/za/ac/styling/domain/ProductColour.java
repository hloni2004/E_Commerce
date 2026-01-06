package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "product", "sizes" })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProductColour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer colourId;

    private String name;
    private String hexCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore // Prevent circular reference
    private Product product;

    @OneToMany(mappedBy = "colour", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ProductColourSize> sizes;
}