package za.ac.styling.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "colour" })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProductColourSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sizeId;

    private String sizeName;
    private int stockQuantity;
    private int reservedQuantity;
    private int reorderLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private ProductColour colour;
}